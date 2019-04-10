package chessgame;

import chessgame.pieces.ChessPiece;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class BoardSquare extends StackPane
{
    int widthAndHeight = 100;
    private final Rectangle rectangle;
    private final Circle highLightCircle;
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
        
        this.highLightCircle = new Circle(widthAndHeight/7, null);
        this.highLightCircle.setFill(Color.CHARTREUSE);
        this.highLightCircle.setOpacity(0.3);
        //this.getChildren().add(this.highLightCircle);
        
        this.color = color;
        this.position = position;

        //this.getChildren().add(this.imageView);
        
        this.setOnDragEntered(getDragEnteredHandler());
        this.setOnDragExited(getDragExitedHandler());
        
        /*Text positionText = new Text(this.position.getX() + "," + this.position.getY());
        positionText.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
        positionText.setStroke(Color.web("#7080A0")); 
        positionText.getTransforms().add(
                new Rotate(180, 50, 50));
        this.getChildren().add(positionText);
        this.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setMargin(positionText, new Insets(0, 10, 0, 0));
        */
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
            if(null != currentPiece)
            {
                this.currentPiece.setIsAlive(false);
                this.getChildren().remove(currentPiece);
            }
            piece.setPosition(this.getPosition());
            this.currentPiece = piece;
            this.getChildren().add(piece);
            isOccupied = true;
        }
        else
        {
            if(null != currentPiece)
            {
                this.getChildren().remove(currentPiece);
            }
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
    
    public void highLight(boolean doHighlight)
    {
        if(doHighlight)
            this.getChildren().add(highLightCircle);
        else
            this.getChildren().remove(highLightCircle);
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
    
    public void clearSquare()
    {
        setCurrentPiece(null);
        setSelected(false);
        highLight(false);
    }
}
