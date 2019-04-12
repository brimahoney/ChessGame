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
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Chess Move - \n");
        builder.append("Team: " + team.getColorName() + "\n");
        builder.append("Piece: " + piece.getName() + "\n");
        builder.append("From - " + fromPosition.toString() + "\n");
        builder.append("To  - " + toPosition.toString() + "\n");
        return builder.toString();
    }
}
