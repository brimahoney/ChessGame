package chessgame.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chessgame.model.BoardSquare;
import chessgame.model.ChessPiece;
import chessgame.model.Piece;
import chessgame.model.Position;
import chessgame.model.TeamColor;

public class MovesCalculator
{
    // Direction vectors: cardinal (N/S/E/W) and diagonal (NE/NW/SE/SW)
    private static final int[][] CARDINAL = {{ 0, 1}, { 0,-1}, { 1, 0}, {-1, 0}};
    private static final int[][] DIAGONAL = {{ 1, 1}, { 1,-1}, {-1, 1}, {-1,-1}};
    private static final int[][] ALL_DIRS = {{ 0, 1}, { 0,-1}, { 1, 0}, {-1, 0},
                                             { 1, 1}, { 1,-1}, {-1, 1}, {-1,-1}};

    private final ChessPiece piece;
    private final BoardSquare[][] board;
    private final ChessPiece[] enemyPieces;

    public MovesCalculator(ChessPiece piece, BoardSquare[][] board, ChessPiece[] enemyPieces)
    {
        this.piece = piece;
        this.board = board;
        this.enemyPieces = enemyPieces;
    }

    public Set<Position> calculate()
    {
        if (!piece.isAlive())
            return Collections.emptySet();

        Set<Position> moves = new HashSet<>();

        switch (piece.getType())
        {
            case KING -> {
                for (int[] d : ALL_DIRS)
                    moves.addAll(slidingMoves(d[0], d[1], 1));
                moves.addAll(calculateCastlingMoves());
                Set<Position> attacked = getEnemyAttackedSquares();
                moves.removeIf(attacked::contains);
            }
            case QUEEN -> {
                for (int[] d : ALL_DIRS)
                    moves.addAll(slidingMoves(d[0], d[1], 7));
            }
            case ROOK -> {
                for (int[] d : CARDINAL)
                    moves.addAll(slidingMoves(d[0], d[1], 7));
            }
            case BISHOP -> {
                for (int[] d : DIAGONAL)
                    moves.addAll(slidingMoves(d[0], d[1], 7));
            }
            case KNIGHT -> moves.addAll(calculateKnightMoves());
            case PAWN   -> moves.addAll(calculatePawnMoves());
        }
        return moves;
    }

    /**
     * Traces a ray from the piece's position in direction (dx, dy), stopping after
     * maxSteps, at the board edge, or when a piece is hit. An enemy on the terminal
     * square is included as a capture; a friendly piece stops the ray without adding.
     */
    private Set<Position> slidingMoves(int dx, int dy, int maxSteps)
    {
        Set<Position> moves = new HashSet<>();
        int x = piece.getPosition().getX() + dx;
        int y = piece.getPosition().getY() + dy;

        for (int steps = 0; steps < maxSteps && isValidPosition(x, y); steps++)
        {
            BoardSquare sq = board[x][y];
            if (sq.isOccupied())
            {
                if (!sq.getCurrentPiece().isFriendly(piece))
                    moves.add(new Position(x, y));
                break;
            }
            moves.add(new Position(x, y));
            x += dx;
            y += dy;
        }
        return moves;
    }

    private Set<Position> calculateKnightMoves()
    {
        Set<Position> moves = new HashSet<>();
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

        moves.removeIf(move ->
            !isValidPosition(move) ||
            (board[move.getX()][move.getY()].isOccupied() &&
             board[move.getX()][move.getY()].getCurrentPiece().isFriendly(piece))
        );
        return moves;
    }

    private Set<Position> calculatePawnMoves()
    {
        Set<Position> moves = new HashSet<>();
        int x = piece.getPosition().getX();
        int y = piece.getPosition().getY();
        int dy = piece.getColor() == TeamColor.WHITE ? 1 : -1;

        // Forward one square — blocked if any piece occupies it
        if (isValidPosition(x, y + dy) && !board[x][y + dy].isOccupied())
        {
            moves.add(new Position(x, y + dy));
            // Double forward on first move — only if the intermediate square was clear
            if (piece.isFirstMove() && isValidPosition(x, y + 2 * dy) && !board[x][y + 2 * dy].isOccupied())
                moves.add(new Position(x, y + 2 * dy));
        }

        // Diagonal captures — only if an enemy occupies the square
        for (int dx : new int[]{-1, 1})
        {
            if (isValidPosition(x + dx, y + dy))
            {
                BoardSquare sq = board[x + dx][y + dy];
                if (sq.isOccupied() && !sq.getCurrentPiece().isFriendly(piece))
                    moves.add(new Position(x + dx, y + dy));
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
        Set<Position> moves = new HashSet<>();

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

    private Set<Position> calculateEnPassantMove()
    {
        // TODO: implement en passant
        return Collections.emptySet();
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

    public static boolean isInCheck(ChessPiece king, ChessPiece[] enemyPieces)
    {
        for (ChessPiece enemy : enemyPieces)
        {
            if (enemy.isAlive() && enemy.getAllowedMoves() != null
                    && enemy.getAllowedMoves().contains(king.getPosition()))
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

    public static boolean isValidPosition(int x, int y)
    {
        return !(x < 0 || x > 7 || y < 0 || y > 7);
    }

    public static boolean isValidPosition(Position p)
    {
        return isValidPosition(p.getX(), p.getY());
    }
}
