package chessgame.pieces;

import chessgame.TeamColor;
import chessgame.Piece;
import chessgame.Position;
import javafx.scene.image.Image;

public class King extends ChessPiece
{
    public King(Position position, TeamColor color, Image image)
    {
        super(position, color, image, Piece.KING);
    }
}
