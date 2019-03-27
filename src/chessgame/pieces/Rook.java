package chessgame.pieces;

import chessgame.TeamColor;
import chessgame.Piece;
import chessgame.Position;
import javafx.scene.image.Image;

public class Rook extends ChessPiece
{
    public Rook(Position position, TeamColor color, Image image)
    {
        super(position, color, image, Piece.ROOK);
    }
}
