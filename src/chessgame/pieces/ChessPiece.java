package chessgame.pieces;

import chessgame.TeamColor;
import chessgame.Piece;
import chessgame.Position;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

public class ChessPiece extends ImageView
{
    private Position position;
    private final TeamColor color;
    private boolean isFirstMove = true;
    private Set<Position> allowedMoves;
            
    private final Piece piece;
    private final int imageViewWidthAndHeight = 80;
    
    private boolean isAlive = true;
    
    private double mouseX = 0;
    private double mouseY = 0;
    private double pieceX = 0;
    private double pieceY = 0;
    

    public ChessPiece(Position position, TeamColor color, Image image, Piece piece)
    {
        super(image);
        this.position = position;
        this.color = color;
        this.piece = piece;
        
        this.getTransforms().add(
                new Rotate(180, imageViewWidthAndHeight/2, imageViewWidthAndHeight/2));
        this.setFitWidth(80);
        this.setPreserveRatio(true);
        this.setSmooth(true);
        this.setCache(true);
        
        this.setOnMousePressed(pressMouse());
	this.setOnMouseDragged(dragMouse());
    }
    
    public Position getPosition()
    {
        return this.position;
    }
    
    public void setPosition(Position position)
    {
        this.position = position;
    }

    public TeamColor getColor()
    {
        return this.color;
    }
    
    public boolean isFirstMove()
    {
        return this.isFirstMove;
    }
    
    public void setMoved()
    {
        this.isFirstMove = false;
    }
    
    public String getImageName()
    {
        return getColor().getColorName() + "_" + this.getName() + ".png";
    }
    
    public boolean isFriendly(ChessPiece piece)
    {
        return piece.getColor().equals(getColor());
    }
    
    public Piece getType()
    {
        return piece;
    }
    
    public String getName()
    {
        return piece.getName();
    }
    
    public double getStrength()
    {
        return piece.getStrength();
    }
    
    public boolean isAlive() 
    {
        return isAlive;
    }

    public void setIsAlive(boolean isAlive) 
    {
        this.isAlive = isAlive;
    }

    private EventHandler<MouseEvent> pressMouse() 
    {
        EventHandler<MouseEvent> mousePressHandler = new EventHandler<MouseEvent>() 
        {

            public void handle(MouseEvent event) 
            {
                if (event.getButton() == MouseButton.PRIMARY) 
                {
                    // get the current mouse coordinates according to the scene.
                    mouseX = event.getSceneX();
                    mouseY = event.getSceneY();

                    // get the current coordinates of the draggable node.
                    pieceX = ChessPiece.this.getLayoutX();
                    pieceY = ChessPiece.this.getLayoutY();
                }
            }
        };
        return mousePressHandler;
    }
    
    private EventHandler<MouseEvent> dragMouse() 
    {
        EventHandler<MouseEvent> dragHandler = (MouseEvent event) -> 
        {
            if(event.getButton() == MouseButton.PRIMARY) 
            {
                // find the delta coordinates by subtracting the new mouse
                // coordinates with the old.
                double deltaX = event.getSceneX() - mouseX;
                double deltaY = event.getSceneY() - mouseY;

                // add the delta coordinates to the node coordinates.
                pieceX += deltaX;
                pieceY += deltaY;

                // set the layout for the draggable node.
                ChessPiece.this.relocate(pieceX, pieceY);

                // get the latest mouse coordinate.
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                System.out.println("dragging " + ChessPiece.this.getName() + 
                        " , pieceX: " + pieceX + ", pieceY:" + pieceY);
            }
        };
        return dragHandler;
    }

    public Set<Position> getAllowedMoves()
    {
        return allowedMoves;
    }

    public void setAllowedMoves(Set<Position> allowedMoves)
    {
        this.allowedMoves = allowedMoves;
    }
    
    public boolean isAllowedMove(Position position)
    {
        return allowedMoves.contains(position);
    }
}
