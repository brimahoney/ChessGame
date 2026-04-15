package chessgame.model;

import java.util.Set;

/**
 * Pure model representation of a chess piece — no JavaFX dependencies.
 * Game logic (moves, state) lives here; rendering lives in ChessPieceView.
 */
public class ChessPiece
{
    private Position position;
    private final TeamColor color;
    private Piece type;
    private boolean isFirstMove = true;
    private boolean isAlive = true;
    private Set<Position> allowedMoves;

    public ChessPiece(Position position, TeamColor color, Piece type)
    {
        this.position = position;
        this.color = color;
        this.type = type;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public TeamColor getColor()
    {
        return color;
    }

    public Piece getType()
    {
        return type;
    }

    public String getName()
    {
        return type.getName();
    }

    public double getStrength()
    {
        return type.getStrength();
    }

    public boolean isFirstMove()
    {
        return isFirstMove;
    }

    public void setMoved()
    {
        isFirstMove = false;
    }

    public boolean isAlive()
    {
        return isAlive;
    }

    public void setIsAlive(boolean isAlive)
    {
        this.isAlive = isAlive;
    }

    public Set<Position> getAllowedMoves()
    {
        return allowedMoves;
    }

    public void setAllowedMoves(Set<Position> allowedMoves)
    {
        this.allowedMoves = allowedMoves;
    }

    public boolean isAllowedMove(Position position)
    {
        return allowedMoves != null && allowedMoves.contains(position);
    }

    public void promote(Piece newType)
    {
        this.type = newType;
    }

    public boolean isFriendly(ChessPiece other)
    {
        return other.getColor().equals(this.color);
    }
}
