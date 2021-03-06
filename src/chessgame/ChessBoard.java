package chessgame;

import chessgame.pieces.ChessPiece;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Rotate;

public class ChessBoard extends GridPane
{
    private BoardSquare[][] squares;
    private Map<Position, BoardSquare> squaresMap;
    
    private BoardSquare selectedSquare;
    private ChessPiece selectedPiece;
    
    private TeamColor turn = TeamColor.WHITE;

    private Squad whiteSquad;
    private Squad blackSquad;
    private final MoveCalcThreadPool moveCalculator;
    
    public ChessBoard()
    {
        createSquares();
        
        whiteSquad = new Squad(TeamColor.WHITE);
        placePieces(whiteSquad);
        blackSquad = new Squad(TeamColor.BLACK);
        placePieces(blackSquad);
        
        moveCalculator = new MoveCalcThreadPool(squares, whiteSquad, blackSquad);
        
        for(int i = 0; i < squares.length; i++)
        {
            for(int j = 0; j < squares[i].length; j++)
            {
                this.add(squares[i][j], 7 - i, j);
            }
        }
        this.getTransforms().add(new Rotate(180, 400, 400));
    }
    
    private void placePieces(Squad squad)
    {
        for (ChessPiece piece : squad.getSquad())
        {
            BoardSquare aSquare = squaresMap.get(piece.getPosition());
            if(null != aSquare)
                aSquare.setCurrentPiece(piece); 
        }
    }
    
    private void createSquares()
    {
        squares = new BoardSquare[8][8];
        squaresMap = new HashMap<>();
        
        for(int i = 0; i < squares.length; i++)
        {
            for(int j = 0; j < squares[i].length; j++)
            {
                //Position p = new Position(file++, rank);
                Position p = new Position(i, j);
                BoardSquare square = new BoardSquare(decideColor(7 - i, j), p);
                square.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() 
                    {
                        public void handle(MouseEvent e) 
                        { 
                            processSelection(square);
                            System.out.println(square.getPosition().toString());
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
        {
            if(square.getCurrentPiece().getColor().equals(turn))
                setSelectedSquare(square, true);
        }
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
                    if(selectedPiece.isAllowedMove(square.getPosition()))
                    {
                        movePiece(selectedPiece, selectedSquare, square);
                    }
                }
                else
                {
                    // take piece when selected and previous selected are not friendly
                    if(!selectedPiece.isFriendly(square.getCurrentPiece())) 
                    {        
                        if(selectedPiece.isAllowedMove(square.getPosition()))
                        {
                            takePiece(selectedPiece, selectedSquare, square);
                        }
                    }
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
        if(selectedPiece.isFirstMove())
            selectedPiece.setMoved();
        endTurn();
    }
    
    private void takePiece(ChessPiece selectedPiece, BoardSquare from, BoardSquare to)
    {
        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
        if(selectedPiece.isFirstMove())
            selectedPiece.setMoved();
        endTurn();
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
                Set<Position> positions = piece.getAllowedMoves();
                positions.stream().forEach((p) ->
                {
                    this.squaresMap.get(p).highLight(true);
                });
            }
        }
        else
        {
            if(null != this.selectedPiece)
            {
                Set<Position> positions = selectedPiece.getAllowedMoves();
                positions.stream().forEach((p) ->
                {
                    this.squaresMap.get(p).highLight(false);
                });
            }
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
    
    public void startGame(TeamColor color)
    {
        this.turn = color;        
        Squad squad = turn.equals(TeamColor.WHITE) ? whiteSquad : blackSquad;
        calculateSquadMoves(squad);
    }
    
    public void startNewGame()
    {
        clearBoard();
        whiteSquad = new Squad(TeamColor.WHITE);
        placePieces(whiteSquad);
        blackSquad = new Squad(TeamColor.BLACK);
        placePieces(blackSquad);
        startGame(TeamColor.WHITE);
    }
    
    public void endTurn()
    {
        this.turn = turn.equals(TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;        
        Squad squad = turn.equals(TeamColor.WHITE) ? whiteSquad : blackSquad;
        calculateSquadMoves(squad);
    }
    
    public void calculateSquadMoves(Squad squad)
    {
        try
        {
            // calculate the moves for the side about to take it's turn 
            List<Future<Set<Position>>> responses = moveCalculator.calculateMoves(squad);
            ChessPiece[] pieces = squad.getSquad();
            int index = 0;
            for(Future<Set<Position>> response : responses)
            {
                try
                {
                    Set<Position> positions = response.get();
                    pieces[index].setAllowedMoves(positions);
                    index++;
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
    
    private void clearBoard()
    {
        for(BoardSquare square : squaresMap.values())
        {
            square.clearSquare();
        }
    }
    
    public void shutdownMovesCalculator()
    {
        System.out.println("Shutting down moves calculator... ");
        this.moveCalculator.shutDown();
    }
}

