package chessgame;

import chessgame.pieces.ChessPiece;
import javafx.event.EventHandler;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class BoardSquare extends StackPane
{
    int widthAndHeight = 100;
    private final Rectangle rectangle;
    private boolean isSelected = false;
    private boolean isOccupied = false;
    
    private ChessPiece currentPiece;
    private final TeamColor color;
    private final Position position;
    
    public BoardSquare(TeamColor color, Position position)
    {
        Paint squareColor = Color.SIENNA;
        if(color.equals(TeamColor.BLACK))
            squareColor = Color.SEASHELL;
        
        this.rectangle = new Rectangle(widthAndHeight, widthAndHeight, squareColor);
        this.rectangle.setStrokeWidth(8);
        this.rectangle.setStrokeType(StrokeType.INSIDE);
        this.getChildren().add(this.rectangle);
        this.color = color;
        this.position = position;

        //this.getChildren().add(this.imageView);
        
        this.setOnDragEntered(getDragEnteredHandler());
        this.setOnDragExited(getDragExitedHandler());
    }
    
    public Position getPosition()
    {
        return this.position;
    }

    public ChessPiece getCurrentPiece()
    {
        return currentPiece;
    }

    public void setCurrentPiece(ChessPiece piece)
    {
        if(null != piece)
        {
            piece.setPosition(this.getPosition());
            this.currentPiece = piece;
            this.getChildren().add(piece);
            isOccupied = true;
        }
        else
        {
            this.currentPiece = null;
            isOccupied = false;
        }                   
    }
    
    public boolean isOccupied()
    {
        return isOccupied;
    }
    
    @Override
    public String toString()
    {
        return "File: " + position.getFile() + " Rank: " + position.getRank() + 
                " Color: " + this.color.getColorName();
    }
    
    public void setSelected(boolean selected)
    {
        if(selected)
            this.rectangle.setStroke(Color.CHARTREUSE);
        else
            this.rectangle.setStroke(null);
        this.isSelected = selected;
    }
    
    public boolean isSelected()
    {
        return this.isSelected;
    }
    
    private EventHandler<DragEvent> getDragEnteredHandler()
    {
        EventHandler<DragEvent> eventHandler = (DragEvent event) -> 
        {
            /* the drag-and-drop gesture entered the target */
            /* show to the user that it is an actual gesture target */ 
            if (event.getGestureSource() != BoardSquare.this)
            {
                BoardSquare.this.rectangle.setStroke(Color.RED);
            }
            
            event.consume();
        };
        return eventHandler;
    }

    private EventHandler<DragEvent> getDragExitedHandler()
    {
        EventHandler<DragEvent> eventHandler = (DragEvent event) -> 
        {
            /* mouse moved away, remove the graphical cues */
            BoardSquare.this.rectangle.setStroke(null);
            
            event.consume();
        };
        return eventHandler;
    }
}
