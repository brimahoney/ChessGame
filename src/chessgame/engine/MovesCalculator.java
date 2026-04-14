package chessgame.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import chessgame.model.BoardSquare;
import chessgame.model.ChessPiece;
import chessgame.model.Piece;
import chessgame.model.Position;
import chessgame.model.TeamColor;

public class MovesCalculator implements Callable<Set<Position>>
{
    private final ChessPiece piece;
    private final BoardSquare[][] board;
    private final ChessPiece[] enemyPieces;

    public MovesCalculator(ChessPiece piece, BoardSquare[][] board, ChessPiece[] enemyPieces)
    {
        this.piece = piece;
        this.board = board;
        this.enemyPieces = enemyPieces;
    }
    
    @Override
    public Set<Position> call() throws Exception 
    {
        HashSet<Position> moves = new HashSet<>();
        
        // return immediately if piece is not active
        if(!this.piece.isAlive())
            return Collections.EMPTY_SET;
        
        switch (piece.getType()) 
        {
            case KING -> {
                moves.addAll(calculateDiagonalMoves(true));
                moves.addAll(calculatePerpendicularMoves(true));
                moves.addAll(calculateCastlingMoves());
                // Remove any square the enemy can attack — king cannot move into check
                Set<Position> attacked = getEnemyAttackedSquares();
                moves.removeIf(attacked::contains);
            }
            case QUEEN -> {
                moves.addAll(calculateDiagonalMoves(false));
                moves.addAll(calculatePerpendicularMoves(false));
            }
            case BISHOP -> moves.addAll(calculateDiagonalMoves(false));
            case KNIGHT -> moves.addAll(calculateKnightMoves());
            case ROOK -> moves.addAll(calculatePerpendicularMoves(false));
            case PAWN -> moves.addAll(calculatePawnMoves());
        }
        return moves;
    }

    private Set<Position> calculateDiagonalMoves(boolean isKing)
    {
        HashSet<Position> moves = new HashSet<>();
        moves.addAll(calculateNorthEastMoves(piece.getPosition(), isKing));
        moves.addAll(calculateNorthWestMoves(piece.getPosition(), isKing));
        moves.addAll(calculateSouthEastMoves(piece.getPosition(), isKing));
        moves.addAll(calculateSouthWestMoves(piece.getPosition(), isKing));
        return moves;
    }
    
    private Set<Position> calculatePerpendicularMoves(boolean isKing)
    {
        HashSet<Position> moves = new HashSet<>();
        moves.addAll(calculateNorthMoves(piece.getPosition(), isKing));
        moves.addAll(calculateSouthMoves(piece.getPosition(), isKing));
        moves.addAll(calculateEastMoves(piece.getPosition(), isKing));        
        moves.addAll(calculateWestMoves(piece.getPosition(), isKing));
        return moves;
    }
    
    private Set<Position> calculateKnightMoves()
    {
        HashSet<Position> moves = new HashSet<>();
        int x = piece.getPosition().getX();
        int y = piece.getPosition().getY();
        
        moves.add(new Position(x - 2, y - 1));
        moves.add(new Position(x - 2, y + 1));
        moves.add(new Position(x + 2, y - 1));
        moves.add(new Position(x + 2, y + 1));
        moves.add(new Position(x - 1, y - 2));
        moves.add(new Position(x - 1, y + 2));
        moves.add(new Position(x + 1, y - 2));
        moves.add(new Position(x + 1, y + 2));

        
        //remove from the moves if not a valid position or 
        // if the square is occupied and the occupier is friendly
        moves.removeIf((Position move) -> 
        {
            if(!isValidPosition(move))
            {
                return true;
            }
            else
            {
                BoardSquare theSquare = board[move.getX()][move.getY()];
                return theSquare.isOccupied() && theSquare.getCurrentPiece().isFriendly(this.piece);
            }
        });
       
        return moves;
    }
    
    private Set<Position> calculatePawnMoves()
    {
        HashSet<Position> moves = new HashSet<>();
        Position position = piece.getPosition();
        boolean wasNorthMove = false;
        boolean wasSouthMove = false;
        
        // if white, can only move North
        if(piece.getColor().equals(TeamColor.WHITE))
        {
            Set<Position> northMoves = calculateNorthMoves(position, true);
            wasNorthMove = !northMoves.isEmpty();
            moves.addAll(northMoves);
            moves.addAll(calculateNorthEastMoves(piece.getPosition(), true));
            moves.addAll(calculateNorthWestMoves(piece.getPosition(), true));
        }
        // else if black, can only move south 
        else
        {
            Set<Position> southMoves = calculateSouthMoves(position, true);
            wasSouthMove = !southMoves.isEmpty();
            moves.addAll(southMoves);
            moves.addAll(calculateSouthEastMoves(piece.getPosition(), true));
            moves.addAll(calculateSouthWestMoves(piece.getPosition(), true));
        }
        
        // if first move of pawn, then add second move forward
        if(piece.isFirstMove())
        {
            int moveDirection = piece.getColor().equals(TeamColor.WHITE) ? 1 : -1;
            int x = position.getX();
            int y = position.getY() + moveDirection;
            Position newPosition = new Position(x, y);
            if(piece.getColor().equals(TeamColor.WHITE))
            {
                if(wasNorthMove)
                    moves.addAll(calculateNorthMoves(newPosition, true));
            }
            else
            {
                if(wasSouthMove)
                    moves.addAll(calculateSouthMoves(newPosition, true));
            }
        }
        return moves;
    }
    
    /**
     * Castling is legal when:
     *   - The king has not previously moved
     *   - The rook on that side has not previously moved
     *   - No pieces stand between the king and the rook
     *   - The king is not currently in check
     *   - The king does not pass through or land on a square attacked by the enemy
     *
     * Enemy attacked squares are derived from the enemy pieces' most recently
     * calculated allowedMoves (updated at the end of each enemy turn).
     */
    private Set<Position> calculateCastlingMoves()
    {
        HashSet<Position> moves = new HashSet<>();

        if (!piece.isFirstMove())
            return moves;

        int x = piece.getPosition().getX();
        int y = piece.getPosition().getY();

        // Sanity check: king must still be on the e-file
        if (x != 4)
            return moves;

        // King must not currently be in check
        if (isInCheck(piece, enemyPieces))
            return moves;

        Set<Position> attacked = getEnemyAttackedSquares();

        // --- Kingside (short) castling ---
        // Rook on h-file (x=7); squares f (x=5) and g (x=6) must be empty and unattacked
        BoardSquare kingsideRookSquare = board[7][y];
        if (kingsideRookSquare.isOccupied())
        {
            ChessPiece kingsideRook = kingsideRookSquare.getCurrentPiece();
            if (kingsideRook.getType() == Piece.ROOK &&
                kingsideRook.getColor().equals(piece.getColor()) &&
                kingsideRook.isFirstMove() &&
                !board[5][y].isOccupied() && !board[6][y].isOccupied() &&
                !attacked.contains(new Position(5, y)) &&
                !attacked.contains(new Position(6, y)))
            {
                moves.add(new Position(6, y));  // king lands on g-file
            }
        }

        // --- Queenside (long) castling ---
        // Rook on a-file (x=0); squares b (x=1), c (x=2), d (x=3) must be empty;
        // c and d must also be unattacked (b is irrelevant for king path)
        BoardSquare queensideRookSquare = board[0][y];
        if (queensideRookSquare.isOccupied())
        {
            ChessPiece queensideRook = queensideRookSquare.getCurrentPiece();
            if (queensideRook.getType() == Piece.ROOK &&
                queensideRook.getColor().equals(piece.getColor()) &&
                queensideRook.isFirstMove() &&
                !board[1][y].isOccupied() && !board[2][y].isOccupied() && !board[3][y].isOccupied() &&
                !attacked.contains(new Position(3, y)) &&
                !attacked.contains(new Position(2, y)))
            {
                moves.add(new Position(2, y));  // king lands on c-file
            }
        }

        return moves;
    }

    /**
     * Returns all squares threatened by the enemy — used to restrict king movement.
     * Pawns are handled separately: their allowedMoves only include occupied diagonal
     * squares (capture targets), but they threaten both diagonal squares ahead whether
     * empty or not. All other pieces threaten exactly their allowedMoves squares.
     */
    private Set<Position> getEnemyAttackedSquares()
    {
        Set<Position> attacked = new HashSet<>();
        for (ChessPiece enemy : enemyPieces)
        {
            if (!enemy.isAlive()) continue;

            if (enemy.getType() == Piece.PAWN)
                attacked.addAll(getPawnAttackSquares(enemy));
            else if (enemy.getAllowedMoves() != null)
                attacked.addAll(enemy.getAllowedMoves());
        }
        return attacked;
    }

    /** Both diagonal squares a pawn threatens, regardless of occupancy. */
    private static Set<Position> getPawnAttackSquares(ChessPiece pawn)
    {
        Set<Position> squares = new HashSet<>();
        int x = pawn.getPosition().getX();
        int y = pawn.getPosition().getY();
        int dy = pawn.getColor() == TeamColor.WHITE ? 1 : -1;
        if (isValidPosition(x - 1, y + dy)) squares.add(new Position(x - 1, y + dy));
        if (isValidPosition(x + 1, y + dy)) squares.add(new Position(x + 1, y + dy));
        return squares;
    }

    private Set<Position> calculateEnPassantMove()
    {
        HashSet<Position> moves = new HashSet<>();
        return moves;
    }
    
    public static boolean isInCheck(ChessPiece king, ChessPiece[] enemyPieces)
    {
        for(ChessPiece enemy : enemyPieces)
        {
            if(enemy.isAlive() && enemy.getAllowedMoves() != null && enemy.getAllowedMoves().contains(king.getPosition()))
                return true;
        }
        return false;
    }

    /** Returns every enemy piece whose allowedMoves include the king's square. */
    public static List<ChessPiece> findAttackers(ChessPiece king, ChessPiece[] enemyPieces)
    {
        List<ChessPiece> attackers = new ArrayList<>();
        for (ChessPiece enemy : enemyPieces)
        {
            if (enemy.isAlive() && enemy.getAllowedMoves() != null
                    && enemy.getAllowedMoves().contains(king.getPosition()))
                attackers.add(enemy);
        }
        return attackers;
    }

    /**
     * Returns the set of positions that, if occupied by a friendly piece, would
     * resolve a check from the given attacker:
     *   - The attacker's own square (capture it)
     *   - Every square between the attacker and the king (block sliding pieces)
     * Knights and pawns cannot be blocked, so only their square is returned.
     */
    public static Set<Position> getCheckResolvingPositions(Position kingPos, ChessPiece attacker)
    {
        Set<Position> resolving = new HashSet<>();
        resolving.add(attacker.getPosition());

        Piece type = attacker.getType();
        if (type == Piece.ROOK || type == Piece.BISHOP || type == Piece.QUEEN)
            resolving.addAll(getSquaresBetween(attacker.getPosition(), kingPos));

        return resolving;
    }

    /** Squares strictly between two positions along a rank, file, or diagonal. */
    private static Set<Position> getSquaresBetween(Position from, Position to)
    {
        Set<Position> squares = new HashSet<>();
        int dx = Integer.signum(to.getX() - from.getX());
        int dy = Integer.signum(to.getY() - from.getY());
        int x = from.getX() + dx;
        int y = from.getY() + dy;
        while (x != to.getX() || y != to.getY())
        {
            squares.add(new Position(x, y));
            x += dx;
            y += dy;
        }
        return squares;
    }

    /**
     * x - 1, y + 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthWestMoves(Position p, boolean isKing)
    {
        int x = p.getX() - 1;
        int y = p.getY() + 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, -1, 1, isKing);
    }

    //private Set<Position> calculateMovesWithPawnCheck(Position p, int xFactor, int yFactor, boolean )
    //{
        
    //}
        
    /**
     * x - 1, y - 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */    
    private Set<Position> calculateSouthWestMoves(Position p, boolean isKing)
    {
        int x = p.getX() - 1;
        int y = p.getY() - 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, -1, -1, isKing);
    }
    
    /**
     * x + 1, y + 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthEastMoves(Position p, boolean isKing)
    {
        int x = p.getX() + 1;
        int y = p.getY() + 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 1, 1, isKing);
    }
    
    /**
     * x + 1, y - 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateSouthEastMoves(Position p, boolean isKing)
    {
        int x = p.getX() + 1;
        int y = p.getY() - 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 1, -1, isKing);
    }

    /**
     * x, y + 1
     * Returns empty set if the moving piece is a pawn and the square directly
     * in front is occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthMoves(Position p, boolean isKing)
    {
        int x = p.getX();
        int y = p.getY() + 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN) && board[x][y].isOccupied()))
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 0, 1, isKing);
    }

    /**
     * x, y - 1
     * Returns empty set if the moving piece is a pawn and the square directly
     * in front is occupied (pawns only attack diagonally)
     */    
    private Set<Position> calculateSouthMoves(Position p, boolean isKing)
    {
        int x = p.getX();
        int y = p.getY() - 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN) && board[x][y].isOccupied()))
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 0, -1, isKing);
    }
    
    /**
     * x + 1, y
     */
    private Set<Position> calculateEastMoves(Position p, boolean isKing)
    {
        return calculateMoves(p, 1, 0, isKing);
    }
    
    /**
     * x - 1, y
     */
    private Set<Position> calculateWestMoves(Position p, boolean isKing)
    {
        return calculateMoves(p, -1, 0, isKing);
    }
    
    private Set<Position> calculateMoves(Position p, int xFactor, int yFactor, boolean isKing)
    {
        int x = p.getX() + xFactor;
        int y = p.getY() + yFactor;        
        Position position = new Position(x, y);
        HashSet<Position> moves = new HashSet<>();
        
        if(!isValidPosition(x, y))
            return moves;
        
        BoardSquare theSquare = board[x][y];
        if(theSquare.isOccupied())
        {
            if(theSquare.getCurrentPiece().isFriendly(this.piece))
            {
                return moves;
            }
            else
            {
                moves.add(position);
                return moves;
            }
        }
        else
            moves.add(position);
        
        if(!isKing)
            moves.addAll(calculateMoves(position, xFactor, yFactor, isKing));
        
        return moves;        
    }
    
    public static boolean isValidPosition(int x, int y)
    {
        return !(x < 0 || x > 7 || y < 0 || y > 7);
    }

    public static boolean isValidPosition(Position p)
    {
        return isValidPosition(p.getX(), p.getY());
    }
}
