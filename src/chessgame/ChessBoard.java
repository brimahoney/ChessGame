package chessgame;

import chessgame.model.ChessPiece;
import chessgame.model.BoardSquare;
import chessgame.pieces.Piece;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class ChessBoard extends GridPane
{
    // Model squares — passed to MovesCalculator (no JavaFX dependency)
    private BoardSquare[][] modelSquares;
    // View squares — added to the GridPane for rendering
    private BoardSquareView[][] viewSquares;
    private Map<Position, BoardSquareView> squaresMap;

    private BoardSquareView selectedSquare;
    private ChessPiece selectedPiece;

    private TeamColor turn = TeamColor.WHITE;

    private ControlsPane controlsPane;
    private Label warningLabel;
    private PauseTransition warningPause;
    private FadeTransition warningFade;

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

        moveCalculator = new MoveCalcThreadPool(modelSquares, whiteSquad, blackSquad);

        for(int i = 0; i < viewSquares.length; i++)
        {
            for(int j = 0; j < viewSquares[i].length; j++)
            {
                this.add(viewSquares[i][j], 7 - i, j);
            }
        }
        this.getTransforms().add(new Rotate(180, 400, 400));
    }

    private void placePieces(Squad squad)
    {
        for (ChessPiece piece : squad.getSquad())
        {
            BoardSquareView aSquare = squaresMap.get(piece.getPosition());
            if(null != aSquare)
                aSquare.setCurrentPiece(piece);
        }
    }

    private void createSquares()
    {
        modelSquares = new BoardSquare[8][8];
        viewSquares = new BoardSquareView[8][8];
        squaresMap = new HashMap<>();

        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                Position p = new Position(i, j);
                BoardSquare modelSquare = new BoardSquare(decideColor(7 - i, j), p);
                BoardSquareView viewSquare = new BoardSquareView(modelSquare);
                viewSquare.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
                    {
                        public void handle(MouseEvent e)
                        {
                            processSelection(viewSquare);
                            System.out.println(viewSquare.getPosition().toString());
                        }
                    });
                modelSquares[i][j] = modelSquare;
                viewSquares[i][j] = viewSquare;
                squaresMap.put(p, viewSquare);
            }
        }
    }

    private void processSelection(BoardSquareView square)
    {
        // select a square/piece (no previous selection)
        if(null == selectedSquare && square.isOccupied())
        {
            if(square.getCurrentPiece().getColor().equals(turn))
            {
                setStatus("");
                setSelectedSquare(square, true);
            }
            else
            {
                setStatus("It's " + turn.getColorName() + "'s turn!");
            }
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

    private void movePiece(ChessPiece selectedPiece, BoardSquareView from, BoardSquareView to)
    {
        setStatus("");

        // Detect castling: king moving two squares horizontally
        if (selectedPiece.getType() == Piece.KING &&
            Math.abs(to.getPosition().getX() - from.getPosition().getX()) == 2)
        {
            executeCastling(selectedPiece, from, to);
            return;
        }

        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
        if(selectedPiece.isFirstMove())
            selectedPiece.setMoved();
        endTurn();
    }

    /**
     * Executes both halves of a castling move: slides the king two squares and
     * jumps the rook to the square the king crossed.
     *   Kingside:  king e→g, rook h→f
     *   Queenside: king e→c, rook a→d
     */
    private void executeCastling(ChessPiece king, BoardSquareView kingFrom, BoardSquareView kingTo)
    {
        int y = kingFrom.getPosition().getY();
        boolean kingside = kingTo.getPosition().getX() == 6;

        BoardSquareView rookFrom = squaresMap.get(new Position(kingside ? 7 : 0, y));
        BoardSquareView rookTo   = squaresMap.get(new Position(kingside ? 5 : 3, y));

        ChessPiece rook = rookFrom.getCurrentPiece();

        // Move king
        kingFrom.setCurrentPiece(null);
        kingTo.setCurrentPiece(king);
        king.setMoved();

        // Move rook
        rookFrom.setCurrentPiece(null);
        rookTo.setCurrentPiece(rook);
        rook.setMoved();

        setSelectedSquare(kingFrom, false);
        endTurn();
    }

    private void takePiece(ChessPiece selectedPiece, BoardSquareView from, BoardSquareView to)
    {
        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
        if(selectedPiece.isFirstMove())
            selectedPiece.setMoved();
        endTurn();
    }

    private void setSelectedSquare(BoardSquareView square, boolean selected)
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

    public void setControlsPane(ControlsPane controlsPane)
    {
        this.controlsPane = controlsPane;
    }

    public void setWarningLabel(Label label)
    {
        this.warningLabel = label;
    }

    private void setStatus(String message)
    {
        if (controlsPane != null)
            controlsPane.setStatus(message);

        if (!message.isEmpty())
            showMessage(message, Duration.seconds(2));
    }

    // Show a floating message over the board.
    // Pass null for displayDuration to keep it on screen until the next message or new game.
    public void showMessage(String message, Duration displayDuration)
    {
        if (warningLabel == null) return;

        if (warningPause != null) warningPause.stop();
        if (warningFade != null) warningFade.stop();

        warningLabel.setText(message);
        warningLabel.setOpacity(1.0);
        warningLabel.setVisible(true);

        if (displayDuration != null)
        {
            warningPause = new PauseTransition(displayDuration);
            warningPause.setOnFinished(e -> {
                warningFade = new FadeTransition(Duration.millis(500), warningLabel);
                warningFade.setFromValue(1.0);
                warningFade.setToValue(0.0);
                warningFade.setOnFinished(fe -> warningLabel.setVisible(false));
                warningFade.play();
            });
            warningPause.play();
        }
    }

    public void startGame(TeamColor color)
    {
        this.turn = color;
        Squad squad = turn.equals(TeamColor.WHITE) ? whiteSquad : blackSquad;
        calculateSquadMoves(squad);
    }

    public void startNewGame(TeamColor startingColor)
    {
        clearBoard();
        whiteSquad = new Squad(TeamColor.WHITE);
        placePieces(whiteSquad);
        blackSquad = new Squad(TeamColor.BLACK);
        placePieces(blackSquad);
        // Refresh tasks so the thread pool references the new squads' pieces
        moveCalculator.createTasks(whiteSquad, blackSquad.getSquad());
        moveCalculator.createTasks(blackSquad, whiteSquad.getSquad());
        startGame(startingColor);
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
            // calculate the moves for the side about to take its turn
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
        for(BoardSquareView square : squaresMap.values())
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
