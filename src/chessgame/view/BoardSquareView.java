package chessgame.view;

import chessgame.model.BoardSquare;
import chessgame.model.ChessPiece;
import chessgame.model.Position;
import chessgame.model.TeamColor;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

/**
 * JavaFX view for a board square. Wraps a model BoardSquare and adds
 * visual rendering (rectangle, selection border, move-dot highlight)
 * and drag-target feedback. No game logic lives here.
 */
public class BoardSquareView extends StackPane
{
    private final int widthAndHeight = 100;
    private final Rectangle rectangle;
    private final Circle highLightCircle;
    private boolean isSelected = false;
    private ChessPieceView currentPieceView;

    private final BoardSquare model;

    public BoardSquareView(BoardSquare model)
    {
        Paint squareColor = Color.SIENNA;
        if (model.getColor().equals(TeamColor.BLACK))
            squareColor = Color.SEASHELL;

        this.rectangle = new Rectangle(widthAndHeight, widthAndHeight, squareColor);
        this.rectangle.setStrokeWidth(8);
        this.rectangle.setStrokeType(StrokeType.INSIDE);
        this.getChildren().add(this.rectangle);

        this.highLightCircle = new Circle(widthAndHeight / 7, null);
        this.highLightCircle.setFill(Color.CHARTREUSE);
        this.highLightCircle.setOpacity(0.6);
        //this.getChildren().add(this.highLightCircle);

        this.model = model;

        this.setOnDragEntered(getDragEnteredHandler());
        this.setOnDragExited(getDragExitedHandler());
    }

    public BoardSquare getModel()
    {
        return model;
    }

    // Delegate model reads so callers don't have to reach through getModel()
    public Position getPosition()          { return model.getPosition(); }
    public boolean isOccupied()            { return model.isOccupied(); }
    public ChessPiece getCurrentPiece()    { return model.getCurrentPiece(); }

    public void setCurrentPiece(ChessPiece piece)
    {
        // Remove old view first
        if (currentPieceView != null)
        {
            this.getChildren().remove(currentPieceView);
            currentPieceView = null;
        }
        // Update model state (marks old piece as dead, sets position, tracks occupancy)
        model.setCurrentPiece(piece);
        // Create new view if a piece is being placed
        if (piece != null)
        {
            currentPieceView = new ChessPieceView(
                    piece, ImageLibrary.getImage(piece.getType(), piece.getColor()));
            this.getChildren().add(currentPieceView);
        }
    }

    public void setSelected(boolean selected)
    {
        if (selected)
            this.rectangle.setStroke(Color.CHARTREUSE);
        else
            this.rectangle.setStroke(null);
        this.isSelected = selected;
    }

    public boolean isSelected()
    {
        return this.isSelected;
    }

    public void highLight(boolean doHighlight)
    {
        if (doHighlight)
            this.getChildren().add(highLightCircle);
        else
            this.getChildren().remove(highLightCircle);
    }

    public void clearSquare()
    {
        setCurrentPiece(null);
        setSelected(false);
        highLight(false);
    }

    private EventHandler<DragEvent> getDragEnteredHandler()
    {
        return (DragEvent event) ->
        {
            if (event.getGestureSource() != BoardSquareView.this)
            {
                System.out.println(event.getGestureSource().toString());
                BoardSquareView.this.rectangle.setStroke(Color.RED);
            }
            event.consume();
        };
    }

    private EventHandler<DragEvent> getDragExitedHandler()
    {
        return (DragEvent event) ->
        {
            System.out.println(event.getGestureSource().toString());
            BoardSquareView.this.rectangle.setStroke(null);
            event.consume();
        };
    }
}
