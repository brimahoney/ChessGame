package chessgame;

import java.io.Serializable;

public class ChessMove implements Serializable
{
    private final Position fromPosition;
    private final Position toPosition;
    private final TeamColor team;
    private final Piece piece;
    
    public ChessMove(Position from, Position to, TeamColor team, Piece piece)
    {
        this.fromPosition = from;
        this.toPosition = to;
        this.team = team;
        this.piece = piece;
    }
    
    public Position getFromPosition()
    {
        return fromPosition;
    }

    public Position getToPosition()
    {
        return toPosition;
    }

    public TeamColor getTeam()
    {
        return team;
    }

    public Piece getPiece()
    {
        return piece;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Chess Move - \n");
        builder.append("Team: " + getTeam().getColorName() + "\n");
        builder.append("Piece: " + getPiece().getName() + "\n");
        builder.append("From - " + getFromPosition().toString() + "\n");
        builder.append("To  - " + getToPosition().toString() + "\n");
        return builder.toString();
    }
}
