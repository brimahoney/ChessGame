package chessgame;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import chessgame.pieces.ChessPiece;
import java.util.Collections;

public class MovesCalculator implements Callable<Set<Position>>
{
    private final ChessPiece piece;
    private final BoardSquare[][] board;
            
    public MovesCalculator(ChessPiece piece, BoardSquare[][] board)
    {
        this.piece = piece;
        this.board = board;
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
            case KING:
                moves.addAll(calculateDiagonalMoves(true));
                moves.addAll(calculatePerpendicularMoves(true));
                break;
            case QUEEN:
                moves.addAll(calculateDiagonalMoves(false));
                moves.addAll(calculatePerpendicularMoves(false));
                break;
            case BISHOP:
                moves.addAll(calculateDiagonalMoves(false));
                break;
            case KNIGHT:
                moves.addAll(calculateKnightMoves());
                break;
            case ROOK:
                moves.addAll(calculatePerpendicularMoves(false));
                break;
            case PAWN:
                moves.addAll(calculatePawnMoves());
                break;
        }
        return moves;
    }

    private Set<Position> calculateDiagonalMoves(boolean limited)
    {
        HashSet<Position> moves = new HashSet<>();
        moves.addAll(calculateNorthEastMoves(piece.getPosition(), limited));
        moves.addAll(calculateNorthWestMoves(piece.getPosition(), limited));
        moves.addAll(calculateSouthEastMoves(piece.getPosition(), limited));
        moves.addAll(calculateSouthWestMoves(piece.getPosition(), limited));
        return moves;
    }
    
    private Set<Position> calculatePerpendicularMoves(boolean limited)
    {
        HashSet<Position> moves = new HashSet<>();
        moves.addAll(calculateNorthMoves(piece.getPosition(), limited));
        moves.addAll(calculateSouthMoves(piece.getPosition(), limited));
        moves.addAll(calculateEastMoves(piece.getPosition(), limited));        
        moves.addAll(calculateWestMoves(piece.getPosition(), limited));
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
    
    private Set<Position> calculateCastlingMove()
    {
        HashSet<Position> moves = new HashSet<>();
        return moves;
    }
    
    private Set<Position> calculateEnPassantMove()
    {
        HashSet<Position> moves = new HashSet<>();
        return moves;
    }
    
    /**
     * x - 1, y + 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthWestMoves(Position p, boolean limited)
    {
        int xFactor = -1;
        int yFactor = 1;
        int x = p.getX() - 1;
        int y = p.getY() + 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, -1, 1, limited);
    }

    //private Set<Position> calculateMovesWithPawnCheck(Position p, int xFactor, int yFactor, boolean )
    //{
        
    //}
        
    /**
     * x - 1, y - 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */    
    private Set<Position> calculateSouthWestMoves(Position p, boolean limited)
    {
        int xFactor = -1;
        int yFactor = -1;
        int x = p.getX() - 1;
        int y = p.getY() - 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, -1, -1, limited);
    }
    
    /**
     * x + 1, y + 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthEastMoves(Position p, boolean limited)
    {
        int xFactor = 1;
        int yFactor = 1;
        int x = p.getX() + 1;
        int y = p.getY() + 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 1, 1, limited);
    }
    
    /**
     * x + 1, y - 1
     * Returns empty set if the moving piece is a pawn and the square diagonal
     * to it is not occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateSouthEastMoves(Position p, boolean limited)
    {
        int xFactor = 1;
        int yFactor = -1;
        int x = p.getX() + 1;
        int y = p.getY() - 1;        

        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN)) && !board[x][y].isOccupied())
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 1, -1, limited);
    }

    /**
     * x, y + 1
     * Returns empty set if the moving piece is a pawn and the square directly
     * in front is occupied (pawns only attack diagonally)
     */
    private Set<Position> calculateNorthMoves(Position p, boolean limited)
    {
        int xFactor = 0;
        int yFactor = 1;
        int x = p.getX();
        int y = p.getY() + 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN) && board[x][y].isOccupied()))
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 0, 1, limited);
    }

    /**
     * x, y - 1
     * Returns empty set if the moving piece is a pawn and the square directly
     * in front is occupied (pawns only attack diagonally)
     */    
    private Set<Position> calculateSouthMoves(Position p, boolean limited)
    {
        int xFactor = 0;
        int yFactor = -1;
        int x = p.getX();
        int y = p.getY() - 1;        
                
        if(!isValidPosition(x, y) ||
                (this.piece.getType().equals(Piece.PAWN) && board[x][y].isOccupied()))
        {
            return Collections.EMPTY_SET;
        }
        else
            return calculateMoves(p, 0, -1, limited);
    }
    
    /**
     * x + 1, y
     */
    private Set<Position> calculateEastMoves(Position p, boolean limited)
    {
        return calculateMoves(p, 1, 0, limited);
    }
    
    /**
     * x - 1, y
     */
    private Set<Position> calculateWestMoves(Position p, boolean limited)
    {
        return calculateMoves(p, -1, 0, limited);
    }
    
    private Set<Position> calculateMoves(Position p, int xFactor, int yFactor, boolean limited)
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
        
        if(!limited)
            moves.addAll(calculateMoves(position, xFactor, yFactor, limited));
        
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
