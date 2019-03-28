package chessgame;

import chessgame.pieces.ChessPiece;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class ChessGame extends Application
{
    private BoardSquare[][] squares;
    private Map<Position, BoardSquare> squaresMap;
    
    private BoardSquare selectedSquare;
    private ChessPiece selectedPiece;
    
    private TeamColor turn = TeamColor.WHITE;

    private final Squad whiteSquad;
    private final Squad blackSquad;
    private final MoveCalcThreadPool moveCalculator;
    
    public ChessGame()
    {
        createSquares();
        
        whiteSquad = new Squad(TeamColor.WHITE);
        blackSquad = new Squad(TeamColor.BLACK);
        
        ChessPiece[] whiteSquadPieces = whiteSquad.getSquad();
        for(int i = 0; i < whiteSquadPieces.length; i++)
        {
            ChessPiece piece = whiteSquadPieces[i];
            BoardSquare aSquare = squaresMap.get(piece.getPosition());
            if(null != aSquare)
                aSquare.setCurrentPiece(piece);
        }
        
        ChessPiece[] blackSquadPieces = blackSquad.getSquad();        
        for(int i = 0; i < blackSquadPieces.length; i++)
        {
            ChessPiece piece = blackSquadPieces[i];
            BoardSquare aSquare = squaresMap.get(piece.getPosition());
            if(null != aSquare)
                aSquare.setCurrentPiece(piece);
        }
        
        moveCalculator = new MoveCalcThreadPool(squares);
    }
    
    @Override
    public void start(Stage primaryStage)
    {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setAlignment(Pos.CENTER); 
        for(int i = 0; i < squares.length; i++)
        {
            for(int j = 0; j < squares[i].length; j++)
            {
                gridPane.add(squares[i][j], i, j);
                //System.out.println(squares[i][j].getCurrentPiece().getImageName());
            }
        }
        gridPane.setBorder(new Border(new BorderStroke(Paint.valueOf("BLACK"), 
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        gridPane.getTransforms().add(new Rotate(-90, 400, 400));
        Scene scene = new Scene(gridPane, 800, 800);

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
    
    private void createSquares()
    {
        squares = new BoardSquare[8][8];
        squaresMap = new HashMap<>();
        
        for(int i = 0; i < squares.length; i++)
        {
            char file = 'a';
            int rank = i + 1;
            for(int j = 0; j < squares[i].length; j++)
            {
                //Position p = new Position(file++, rank);
                Position p = new Position(i, j);
                BoardSquare square = new BoardSquare(decideColor(i, j), p);
                square.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() 
                    {
                        public void handle(MouseEvent e) 
                        { 
                            processSelection(square);
                        }
                    });
                squares[i][j] = square;
                squaresMap.put(p, square);
            }
        }
    }

    private void processSelection(BoardSquare square)
    {
        // select a square/piece (no previous selection)
        if(null == selectedSquare && square.isOccupied())
            setSelectedSquare(square, true);
        else if(null != selectedSquare)
        {
            // deselect square (same square selected)
            if(square.equals(selectedSquare))
                setSelectedSquare(square, false);
            // select a different square (no piece on previous)
            else if(null == selectedPiece)
            {
                setSelectedSquare(selectedSquare, false);
                setSelectedSquare(square, true);
            }
            else if(null != selectedPiece)
            {
                // move piece to vacant square
                if(null == square.getCurrentPiece())
                {
                    movePiece(selectedPiece, selectedSquare, square);
                    endTurn();
                }
                else
                {
                    // take piece when selected and previous selected are not friendly
                    if(!selectedPiece.isFriendly(square.getCurrentPiece()))
                        takePiece(selectedPiece, selectedSquare, square);
                    else
                    {
                        // select another square/piece when previous selection was friendly
                        setSelectedSquare(selectedSquare, false);
                        setSelectedSquare(square, true);
                    }
                }
            }
        }
    }
    
    private void movePiece(ChessPiece selectedPiece, BoardSquare from, BoardSquare to)
    {
        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
    }
    
    private void takePiece(ChessPiece selectedPiece, BoardSquare from, BoardSquare to)
    {
        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
    }
                    
    private void setSelectedSquare(BoardSquare square, boolean selected)
    {
        if(selected)
        {
            square.setSelected(true);
            this.selectedSquare = square;
            ChessPiece piece = this.selectedSquare.getCurrentPiece();
            if(null != piece)
            {
                this.selectedPiece = piece;
            }
        }
        else
        {
            square.setSelected(false);
            this.selectedSquare = null;
            this.selectedPiece = null;
        }
    }
    
    private TeamColor decideColor(int i, int j)
    {
        if(i % 2 == 0)
        {
            if(j % 2 == 0)
                return TeamColor.BLACK;
            else
                return TeamColor.WHITE;
        }
        else
        {
            if(j % 2 == 0)
                return TeamColor.WHITE;
            else
                return TeamColor.BLACK;
        }
    }
    
    public void endTurn()
    {
        this.turn = turn.equals(TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;        
        Squad squad = turn.equals(TeamColor.WHITE) ? whiteSquad : blackSquad;
        try
        {
            List<Future<Set<Position>>> responses = moveCalculator.calculateMoves(squad);
            ChessPiece[] pieces = squad.getSquad();
            int index = 0;
            for(Future<Set<Position>> response : responses)
            {
                try
                {
                    while(!response.isDone())
                    {
                        try
                        {
                            Thread.sleep(10);
                        }
                        catch(InterruptedException ie)
                        {
                            ie.printStackTrace();
                        }
                    }
                    Set<Position> positions = response.get();
                   // System.out.println(pieces[index].getColor().getColorName() + " " +
                     //       pieces[index].getName() + " :-----------------------");
                    index++;
                    for(Position position : positions)
                    {
                        //System.out.println(position.toString());
                    }
                }
                catch(ExecutionException ee)
                {
                    ee.printStackTrace();
                }
                
            }
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
    }
    
    @Override
    public void stop()
    {
        this.moveCalculator.shutDown();
    }
}
