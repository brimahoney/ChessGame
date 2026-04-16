package chessgame.view;

import chessgame.model.BoardSquare;
import chessgame.model.ChessPiece;
import chessgame.model.Position;
import chessgame.model.TeamColor;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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
    private final Rectangle rectangle;
    private final Circle highLightCircle;
    private boolean isSelected = false;
    private ChessPieceView currentPieceView;

    private final BoardSquare model;

    public BoardSquareView(BoardSquare model)
    {
        // TeamColor.WHITE = dark squares, TeamColor.BLACK = light squares (classic wood tones)
        boolean isLightSquare = model.getColor().equals(TeamColor.BLACK);

        // Diagonal gradient creates the main grain banding (light/dark alternating bands)
        LinearGradient woodGrain = isLightSquare
            ? new LinearGradient(0.05, 0, 0.9, 0.75, true, CycleMethod.REFLECT,
                new Stop(0, Color.web("#F8E4C2")), new Stop(1, Color.web("#DFC490")))
            : new LinearGradient(0.05, 0, 0.9, 0.75, true, CycleMethod.REFLECT,
                new Stop(0, Color.web("#C49878")), new Stop(1, Color.web("#9A6640")));

        this.rectangle = new Rectangle();
        this.rectangle.widthProperty().bind(this.widthProperty());
        this.rectangle.heightProperty().bind(this.heightProperty());
        this.rectangle.setFill(woodGrain);
        this.rectangle.strokeWidthProperty().bind(this.widthProperty().multiply(0.08));
        this.rectangle.setStrokeType(StrokeType.INSIDE);
        this.getChildren().add(this.rectangle);

        // Dot color adapts to the square: dark dot on light squares, light dot on dark squares
        Color dotColor = isLightSquare ? Color.color(0, 0, 0, 0.25) : Color.color(1, 1, 1, 0.22);
        this.highLightCircle = new Circle(20, dotColor);
        this.highLightCircle.radiusProperty().bind(this.widthProperty().divide(5));

        this.model = model;

        // Hint so the GridPane has an initial size to work with; the rectangle bindings
        // take over once the layout system assigns actual column/row widths.
        this.setPrefSize(100, 100);
        this.setMinSize(0, 0);

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
            bindPieceSize(currentPieceView);
            this.getChildren().add(currentPieceView);
        }
    }

    public void setSelected(boolean selected)
    {
        if (selected)
            this.rectangle.setStroke(Color.web("#F6F669"));
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

    /**
     * Recreates the piece image view using the currently loaded icon set.
     * Reads piece data from the model without any side effects on model state.
     * Call this on every square after ImageLibrary.loadIconSet() switches sets.
     */
    public void refreshPieceView()
    {
        if (currentPieceView != null)
        {
            this.getChildren().remove(currentPieceView);
            currentPieceView = null;
        }
        ChessPiece piece = model.getCurrentPiece();
        if (piece != null)
        {
            currentPieceView = new ChessPieceView(
                    piece, ImageLibrary.getImage(piece.getType(), piece.getColor()));
            bindPieceSize(currentPieceView);
            this.getChildren().add(currentPieceView);
        }
    }

    public void clearSquare()
    {
        setCurrentPiece(null);
        setSelected(false);
        highLight(false);
    }

    /** Binds a piece image's display size to 90% of this square's actual size. */
    private void bindPieceSize(ChessPieceView pieceView)
    {
        pieceView.fitWidthProperty().bind(this.widthProperty().multiply(0.9));
        pieceView.fitHeightProperty().bind(this.heightProperty().multiply(0.9));
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
