package chessgame;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import chessgame.pieces.ChessPiece;

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
        System.out.println("Thread: " + Thread.currentThread().getId() +
                        " calculating moves for " + this.piece.getName());
        
        HashSet<Position> moves = new HashSet<>();
        switch (piece.getPiece()) 
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
                moves.addAll(calculateDiagonalMoves(false));
                break;
            case PAWN:
                moves.addAll(calculatePawnMoves());
                for(Position p : moves)
                {
                    System.out.println(p.toString());
                }
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
        return moves;
    }
    
    private Set<Position> calculatePawnMoves()
    {
        HashSet<Position> moves = new HashSet<>();
        moves.addAll(calculateNorthMoves(piece.getPosition(), true));
        int x = piece.getPosition().getX();
        int y = piece.getPosition().getY() + 1;
        Position position = new Position(x, y);
        moves.addAll(calculateNorthMoves(position, true));
        return moves;
    }
    
    private Set<Position> calculateCastlingMove()
    {
        HashSet<Position> moves = new HashSet<>();
        return moves;
    }
    
    /**
     * x - 1, y + 1
     */
    private Set<Position> calculateNorthWestMoves(Position p, boolean limited)
    {
        return calculateMoves(p, -1, 1, limited);
    }

    /**
     * x - 1, y - 1
     */    
    private Set<Position> calculateSouthWestMoves(Position p, boolean limited)
    {
        return calculateMoves(p, -1, -1, limited);
    }
    
    /**
     * x + 1, y + 1
     */
    private Set<Position> calculateNorthEastMoves(Position p, boolean limited)
    {
        return calculateMoves(p, 1, 1, limited);
    }
    
    /**
     * x + 1, y - 1
     */
    private Set<Position> calculateSouthEastMoves(Position p, boolean limited)
    {
        return calculateMoves(p, 1, -1, limited);
    }

    /**
     * x, y + 1
     */
    private Set<Position> calculateNorthMoves(Position p, boolean limited)
    {
        return calculateMoves(p, 0, 1, limited);
    }

    /**
     * x, y - 1
     */    
    private Set<Position> calculateSouthMoves(Position p, boolean limited)
    {
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
        
        if(x < 0 || x > 7 || y < 0 || y > 7)
            return moves;
        
        BoardSquare theSquare = board[x][y];
        if(theSquare.isOccupied())
        {
            if(theSquare.getCurrentPiece().isFriendly(this.piece))
                return moves;
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
}
