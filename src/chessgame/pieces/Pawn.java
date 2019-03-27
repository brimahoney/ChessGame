package chessgame.pieces;

import chessgame.TeamColor;
import chessgame.Piece;
import chessgame.Position;
import javafx.scene.image.Image;

public class Pawn extends ChessPiece
{
    public Pawn(Position position, TeamColor color, Image image)
    {
        super(position, color, image, Piece.PAWN);
    }
}
