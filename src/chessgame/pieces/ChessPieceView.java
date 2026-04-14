package chessgame.pieces;

import chessgame.model.ChessPiece;
import chessgame.Position;
import chessgame.TeamColor;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

/**
 * JavaFX view for a chess piece. Wraps a model ChessPiece and adds
 * image rendering and drag interaction. No game logic lives here.
 */
public class ChessPieceView extends ImageView
{
    private final ChessPiece model;
    private final int imageViewWidthAndHeight = 80;

    private double mouseX = 0;
    private double mouseY = 0;
    private double pieceX = 0;
    private double pieceY = 0;

    public ChessPieceView(ChessPiece model, Image image)
    {
        super(image);
        this.model = model;

        this.getTransforms().add(
                new Rotate(180, imageViewWidthAndHeight / 2, imageViewWidthAndHeight / 2));
        this.setFitWidth(80);
        this.setPreserveRatio(true);
        this.setSmooth(true);
        this.setCache(true);

        this.setOnMousePressed(pressMouse());
        this.setOnMouseDragged(dragMouse());
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

    private EventHandler<MouseEvent> pressMouse()
    {
        return (MouseEvent event) ->
        {
            if (event.getButton() == MouseButton.PRIMARY)
            {
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                pieceX = ChessPieceView.this.getLayoutX();
                pieceY = ChessPieceView.this.getLayoutY();
            }
        };
    }

    private EventHandler<MouseEvent> dragMouse()
    {
        return (MouseEvent event) ->
        {
            if (event.getButton() == MouseButton.PRIMARY)
            {
                double deltaX = event.getSceneX() - mouseX;
                double deltaY = event.getSceneY() - mouseY;
                pieceX += deltaX;
                pieceY += deltaY;
                ChessPieceView.this.relocate(pieceX, pieceY);
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                System.out.println("dragging " + ChessPieceView.this.getName()
                        + " , pieceX: " + pieceX + ", pieceY:" + pieceY);
            }
        };
    }
}
