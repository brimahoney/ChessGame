package chessgame.view;

import chessgame.model.ChessPiece;
import chessgame.model.Piece;
import chessgame.model.Position;
import chessgame.model.TeamColor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * JavaFX view for a chess piece. Wraps a model ChessPiece and renders its image.
 * Drag interaction is handled at the ChessBoard level.
 */
public class ChessPieceView extends ImageView
{
    private final ChessPiece model;

    public ChessPieceView(ChessPiece model, Image image)
    {
        super(image);
        this.model = model;
        this.setFitWidth(90);
        this.setFitHeight(90);
        this.setPreserveRatio(true);
        this.setSmooth(true);
        this.setCache(true);
    }

    public ChessPiece getModel()
    {
        return model;
    }

    // Convenience delegates so callers don't need to reach through getModel()
    public Position getPosition()      { return model.getPosition(); }
    public TeamColor getColor()        { return model.getColor(); }
    public Piece getType()             { return model.getType(); }
    public String getName()            { return model.getName(); }
}
