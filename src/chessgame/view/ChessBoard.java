package chessgame.view;

import chessgame.engine.MovesCalculator;
import chessgame.model.BoardSquare;
import chessgame.model.ChessPiece;
import chessgame.model.Piece;
import chessgame.model.Position;
import chessgame.model.Squad;
import chessgame.model.TeamColor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    // Drag state
    private ChessPieceView dragging;
    private BoardSquareView dragSource;
    private double dragOriginX, dragOriginY;
    private boolean isDragging;

    private ControlsPane controlsPane;
    private Label warningLabel;
    private PauseTransition warningPause;
    private FadeTransition warningFade;
    private PromotionPane promotionPane;
    private MoveHistoryPane moveHistoryPane;
    private String pendingMoveNotation;
    private boolean awaitingPromotion;
    private boolean gameOver;
    private int halfMoveClock; // resets on pawn move or capture; draw at 100 (50 moves each)

    private Squad whiteSquad;
    private Squad blackSquad;

    // The square a pawn may capture into via en passant this turn (null if none).
    // Set when a pawn double-moves; cleared at the start of every move.
    private Position enPassantTarget;

    public ChessBoard()
    {
        createSquares();

        whiteSquad = new Squad(TeamColor.WHITE);
        placePieces(whiteSquad);
        blackSquad = new Squad(TeamColor.BLACK);
        placePieces(blackSquad);

        // Layout: column = file (a=0 … h=7), row = 7-rank (rank 8 at top, rank 1 at bottom)
        for (int i = 0; i < viewSquares.length; i++)
        {
            for (int j = 0; j < viewSquares[i].length; j++)
            {
                this.add(viewSquares[i][j], i, 7 - j);
            }
        }

        // Coordinate labels — rank numbers on the right, file letters along the bottom
        for (int row = 0; row < 8; row++)
        {
            Label rankLabel = new Label(String.valueOf(8 - row));
            rankLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
            rankLabel.setTextFill(Color.web("#d4b483"));
            rankLabel.setPrefWidth(26);
            rankLabel.setPrefHeight(100);
            rankLabel.setPadding(new Insets(0, 0, 0, 8));
            GridPane.setValignment(rankLabel, VPos.CENTER);
            GridPane.setHalignment(rankLabel, HPos.CENTER);
            this.add(rankLabel, 8, row);
        }
        for (int col = 0; col < 8; col++)
        {
            Label fileLabel = new Label(String.valueOf((char)('a' + col)));
            fileLabel.setFont(Font.font("SansSerif", FontWeight.BOLD, 13));
            fileLabel.setTextFill(Color.web("#d4b483"));
            fileLabel.setPrefWidth(100);
            fileLabel.setPrefHeight(26);
            fileLabel.setAlignment(Pos.CENTER);
            GridPane.setHalignment(fileLabel, HPos.CENTER);
            this.add(fileLabel, col, 8);
        }

        this.setStyle("-fx-background-color: #3d2b1f;");
        this.setPadding(new Insets(10));

        // Drag handling — intercept at board level so pieces float above all squares
        this.addEventFilter(MouseEvent.MOUSE_PRESSED,  this::onDragStart);
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED,  this::onDragMove);
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onDragEnd);
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
                        @Override
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
        if (awaitingPromotion || gameOver) return;

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
                            movePiece(selectedPiece, selectedSquare, square);
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
        enPassantTarget = null;

        // Capture state BEFORE the move — needed for notation and fifty-move clock
        Piece originalType = selectedPiece.getType();
        boolean isEnPassantCapture = originalType == Piece.PAWN
                && from.getPosition().getX() != to.getPosition().getX()
                && !to.isOccupied();
        boolean isCapture = to.isOccupied() || isEnPassantCapture;

        // Fifty-move clock
        if (originalType == Piece.PAWN || isCapture)
            halfMoveClock = 0;
        else
            halfMoveClock++;

        // Castling
        if (originalType == Piece.KING &&
            Math.abs(to.getPosition().getX() - from.getPosition().getX()) == 2)
        {
            executeCastling(selectedPiece, from, to);
            return;
        }

        // En passant: remove the captured pawn
        if (isEnPassantCapture)
        {
            BoardSquareView capturedSquare = squaresMap.get(
                    new Position(to.getPosition().getX(), from.getPosition().getY()));
            if (capturedSquare != null) capturedSquare.setCurrentPiece(null);
        }

        from.setCurrentPiece(null);
        to.setCurrentPiece(selectedPiece);
        setSelectedSquare(from, false);
        setSelectedSquare(to, false);
        if (selectedPiece.isFirstMove())
            selectedPiece.setMoved();

        // Set en passant target for opponent's next turn
        if (originalType == Piece.PAWN &&
            Math.abs(to.getPosition().getY() - from.getPosition().getY()) == 2)
        {
            int midY = (from.getPosition().getY() + to.getPosition().getY()) / 2;
            enPassantTarget = new Position(to.getPosition().getX(), midY);
        }

        // Pawn promotion
        int promotionRank = selectedPiece.getColor() == TeamColor.WHITE ? 7 : 0;
        if (originalType == Piece.PAWN && to.getPosition().getY() == promotionRank)
        {
            if (promotionPane != null)
            {
                awaitingPromotion = true;
                Position fromPos = from.getPosition();
                Position toPos   = to.getPosition();
                promotionPane.show(selectedPiece.getColor(), chosenType -> {
                    pendingMoveNotation = buildNotation(Piece.PAWN, fromPos, toPos, isCapture, isEnPassantCapture, chosenType);
                    selectedPiece.promote(chosenType);
                    to.setCurrentPiece(null);
                    to.setCurrentPiece(selectedPiece);
                    awaitingPromotion = false;
                    endTurn();
                });
                return;
            }
            selectedPiece.promote(Piece.QUEEN);
            pendingMoveNotation = buildNotation(Piece.PAWN, from.getPosition(), to.getPosition(), isCapture, isEnPassantCapture, Piece.QUEEN);
            endTurn();
            return;
        }

        pendingMoveNotation = buildNotation(originalType, from.getPosition(), to.getPosition(), isCapture, isEnPassantCapture, null);
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

        pendingMoveNotation = kingside ? "O-O" : "O-O-O";
        setSelectedSquare(kingFrom, false);
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
        return (i + j) % 2 == 0 ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public void setControlsPane(ControlsPane controlsPane)
    {
        this.controlsPane = controlsPane;
    }

    public void setWarningLabel(Label label)
    {
        this.warningLabel = label;
    }

    public void setPromotionPane(PromotionPane pane)
    {
        this.promotionPane = pane;
    }

    public void setMoveHistoryPane(MoveHistoryPane pane)
    {
        this.moveHistoryPane = pane;
    }

    private void setStatus(String message)
    {
        if (controlsPane != null)
            controlsPane.setStatus(message);

        if (!message.isEmpty())
            showMessage(message, Duration.seconds(2));
        else if (warningLabel != null)
            warningLabel.setVisible(false);
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
        if (controlsPane != null) controlsPane.setTurn(turn);
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
        startGame(startingColor);
    }

    public void endTurn()
    {
        this.turn = turn.equals(TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        if (controlsPane != null) controlsPane.setTurn(turn);
        Squad squad = turn.equals(TeamColor.WHITE) ? whiteSquad : blackSquad;
        Squad enemy = turn.equals(TeamColor.WHITE) ? blackSquad : whiteSquad;
        // Recalculate the enemy's moves first so they reflect the piece that just moved.
        // This ensures isInCheck sees current attacked squares when we check below
        // and when the active player's castling moves are evaluated.
        calculateSquadMoves(enemy);
        calculateSquadMoves(squad);
        checkGameState(squad, enemy);
    }

    private void checkGameState(Squad currentSquad, Squad enemySquad)
    {
        int totalMoves = 0;
        for (ChessPiece piece : currentSquad.getSquad())
            if (piece.isAlive() && piece.getAllowedMoves() != null)
                totalMoves += piece.getAllowedMoves().size();

        ChessPiece king = null;
        for (ChessPiece piece : currentSquad.getSquad())
            if (piece.isAlive() && piece.getType() == Piece.KING)
                { king = piece; break; }

        boolean inCheck = king != null && MovesCalculator.isInCheck(king, enemySquad.getSquad());
        boolean isMate  = totalMoves == 0 && inCheck;

        // Record the completed move with check/checkmate annotation
        if (pendingMoveNotation != null && moveHistoryPane != null)
        {
            String suffix = isMate ? "#" : inCheck ? "+" : "";
            moveHistoryPane.addMove(pendingMoveNotation + suffix, enemySquad.getColor());
            pendingMoveNotation = null;
        }

        if (totalMoves == 0)
        {
            gameOver = true;
            if (controlsPane != null) controlsPane.stopClock();
            if (inCheck)
                showMessage(enemySquad.getColor().getColorName() + " wins by checkmate!", null);
            else
                showMessage("Stalemate — it's a draw!", null);
            return;
        }

        if (inCheck)
        {
            showMessage("Check!", null);
            return;
        }

        if (halfMoveClock >= 100)
        {
            gameOver = true;
            if (controlsPane != null) controlsPane.stopClock();
            showMessage("Draw — fifty-move rule", null);
            return;
        }

        if (isInsufficientMaterial())
        {
            gameOver = true;
            if (controlsPane != null) controlsPane.stopClock();
            showMessage("Draw — insufficient material", null);
        }
    }

    /**
     * Returns true when neither side has enough material to deliver checkmate.
     * Drawn cases: K vs K, K vs K+B, K vs K+N, K+B vs K+B (same-colored bishops).
     */
    private boolean isInsufficientMaterial()
    {
        int wCount = 0, bCount = 0;
        ChessPiece whiteBishop = null, blackBishop = null;
        boolean wHasHeavy = false, bHasHeavy = false;

        for (ChessPiece p : whiteSquad.getSquad())
        {
            if (!p.isAlive()) continue;
            wCount++;
            if (p.getType() == Piece.BISHOP) whiteBishop = p;
            if (p.getType() == Piece.QUEEN || p.getType() == Piece.ROOK || p.getType() == Piece.PAWN)
                wHasHeavy = true;
        }
        for (ChessPiece p : blackSquad.getSquad())
        {
            if (!p.isAlive()) continue;
            bCount++;
            if (p.getType() == Piece.BISHOP) blackBishop = p;
            if (p.getType() == Piece.QUEEN || p.getType() == Piece.ROOK || p.getType() == Piece.PAWN)
                bHasHeavy = true;
        }

        if (wHasHeavy || bHasHeavy) return false;

        // K vs K
        if (wCount == 1 && bCount == 1) return true;

        // K vs K+B  or  K vs K+N
        if (wCount == 1 && bCount == 2) return true;
        if (bCount == 1 && wCount == 2) return true;

        // K+B vs K+B — draw only if both bishops are on the same colored square
        if (wCount == 2 && bCount == 2 && whiteBishop != null && blackBishop != null)
        {
            int wSquare = (whiteBishop.getPosition().getX() + whiteBishop.getPosition().getY()) % 2;
            int bSquare = (blackBishop.getPosition().getX() + blackBishop.getPosition().getY()) % 2;
            return wSquare == bSquare;
        }

        return false;
    }

    public void calculateSquadMoves(Squad squad)
    {
        Squad enemySquad = squad.getColor() == TeamColor.WHITE ? blackSquad : whiteSquad;
        ChessPiece[] enemyPieces = enemySquad.getSquad();
        for (ChessPiece piece : squad.getSquad())
            piece.setAllowedMoves(new MovesCalculator(piece, modelSquares, enemyPieces, enPassantTarget).calculate());
        filterMovesForCheck(squad, enemySquad);
    }

    /**
     * Filters every piece's allowed moves to only those that do not leave the king
     * in check.  Handles pins, double-checks, and all other cases by simulating each
     * candidate move on the model board and testing whether the king is attacked.
     */
    private void filterMovesForCheck(Squad squad, Squad enemySquad)
    {
        ChessPiece king = null;
        for (ChessPiece piece : squad.getSquad())
            if (piece.isAlive() && piece.getType() == Piece.KING)
                { king = piece; break; }
        if (king == null) return;

        ChessPiece[] enemies = enemySquad.getSquad();
        for (ChessPiece piece : squad.getSquad())
        {
            if (!piece.isAlive()) continue;
            Set<Position> legal = new HashSet<>();
            for (Position to : piece.getAllowedMoves())
            {
                if (!leavesKingInCheck(piece, to, king, enemies))
                    legal.add(to);
            }
            piece.setAllowedMoves(legal);
        }
    }

    /**
     * Simulates moving piece to 'to' on the model board (without side effects),
     * then checks whether the king would be in check in that position.
     * Also validates castling transit squares.
     */
    private boolean leavesKingInCheck(ChessPiece piece, Position to,
                                       ChessPiece king, ChessPiece[] enemies)
    {
        Position from = piece.getPosition();
        int fx = from.getX(), fy = from.getY();
        int tx = to.getX(),   ty = to.getY();

        // En passant: the captured pawn sits on the same file as 'to' but same rank as 'from'
        boolean isEnPassant = piece.getType() == Piece.PAWN
                && fx != tx && !modelSquares[tx][ty].isOccupied();
        int epx = tx, epy = fy;

        // Save
        ChessPiece savedFrom = modelSquares[fx][fy].getCurrentPiece();
        ChessPiece savedTo   = modelSquares[tx][ty].getCurrentPiece();
        ChessPiece savedEp   = isEnPassant ? modelSquares[epx][epy].getCurrentPiece() : null;

        // Apply (no side effects — piece.getPosition() is NOT updated)
        modelSquares[fx][fy].setCurrentPieceDirect(null);
        modelSquares[tx][ty].setCurrentPieceDirect(piece);
        if (isEnPassant) modelSquares[epx][epy].setCurrentPieceDirect(null);

        Position kingPos = piece.getType() == Piece.KING ? to : king.getPosition();
        boolean inCheck = isKingAttackedDirect(kingPos, enemies);

        // For castling also check that the king doesn't pass through an attacked square
        if (!inCheck && piece.getType() == Piece.KING && Math.abs(tx - fx) == 2)
        {
            int transitX = fx + Integer.signum(tx - fx);
            modelSquares[tx][ty].setCurrentPieceDirect(null);
            modelSquares[transitX][fy].setCurrentPieceDirect(piece);
            inCheck = isKingAttackedDirect(new Position(transitX, fy), enemies);
            modelSquares[transitX][fy].setCurrentPieceDirect(null);
            modelSquares[tx][ty].setCurrentPieceDirect(piece);
        }

        // Restore
        modelSquares[fx][fy].setCurrentPieceDirect(savedFrom);
        modelSquares[tx][ty].setCurrentPieceDirect(savedTo);
        if (isEnPassant) modelSquares[epx][epy].setCurrentPieceDirect(savedEp);

        return inCheck;
    }

    /**
     * Directly checks whether kingPos is attacked by any live enemy piece
     * given the current state of modelSquares (post-simulation).
     * Does NOT use stored allowedMoves — reads the board directly.
     */
    private boolean isKingAttackedDirect(Position kingPos, ChessPiece[] enemies)
    {
        int kx = kingPos.getX(), ky = kingPos.getY();
        for (ChessPiece enemy : enemies)
        {
            if (!enemy.isAlive()) continue;
            int ex = enemy.getPosition().getX(), ey = enemy.getPosition().getY();
            // Skip enemies that have been displaced from their square by the simulation
            if (modelSquares[ex][ey].getCurrentPiece() != enemy) continue;

            boolean attacks = switch (enemy.getType())
            {
                case PAWN -> {
                    int dy = enemy.getColor() == TeamColor.WHITE ? 1 : -1;
                    yield ky == ey + dy && (kx == ex - 1 || kx == ex + 1);
                }
                case KNIGHT -> {
                    int adx = Math.abs(kx - ex), ady = Math.abs(ky - ey);
                    yield (adx == 1 && ady == 2) || (adx == 2 && ady == 1);
                }
                case KING   -> Math.abs(kx - ex) <= 1 && Math.abs(ky - ey) <= 1;
                case ROOK   -> (kx == ex || ky == ey) && isRayClearOnBoard(ex, ey, kx, ky);
                case BISHOP -> Math.abs(kx - ex) == Math.abs(ky - ey) && isRayClearOnBoard(ex, ey, kx, ky);
                case QUEEN  -> (kx == ex || ky == ey || Math.abs(kx - ex) == Math.abs(ky - ey))
                               && isRayClearOnBoard(ex, ey, kx, ky);
            };
            if (attacks) return true;
        }
        return false;
    }

    /** True if no piece occupies any square strictly between (fromX,fromY) and (toX,toY). */
    private boolean isRayClearOnBoard(int fromX, int fromY, int toX, int toY)
    {
        int dx = Integer.signum(toX - fromX), dy = Integer.signum(toY - fromY);
        int x = fromX + dx, y = fromY + dy;
        while (x != toX || y != toY)
        {
            if (modelSquares[x][y].isOccupied()) return false;
            x += dx;
            y += dy;
        }
        return true;
    }

    private void onDragStart(MouseEvent event)
    {
        if (event.getButton() != MouseButton.PRIMARY) return;

        // Walk up from the click target to find a ChessPieceView
        Node target = (Node) event.getTarget();
        while (target != null && !(target instanceof ChessPieceView) && target != this)
            target = target.getParent();

        if (!(target instanceof ChessPieceView)) return;

        ChessPieceView pieceView = (ChessPieceView) target;
        if (!pieceView.getColor().equals(turn)) return;

        dragging   = pieceView;
        dragSource = (BoardSquareView) pieceView.getParent();
        isDragging = false;

        // Record the source square's top-left corner in board-local coordinates.
        // Used in onDragMove to offset the translate so the piece centres under the cursor.
        Point2D origin = this.sceneToLocal(dragSource.localToScene(0, 0));
        dragOriginX = origin.getX();
        dragOriginY = origin.getY();
        // Do NOT consume — the MOUSE_PRESSED handler on BoardSquareView still runs
        // processSelection(), which handles selection state and move-dot highlighting.
    }

    private void onDragMove(MouseEvent event)
    {
        if (dragging == null || event.getButton() != MouseButton.PRIMARY) return;

        if (!isDragging)
        {
            isDragging = true;
            dragSource.toFront();   // render source square above all siblings
        }

        Point2D mouse = this.sceneToLocal(event.getSceneX(), event.getSceneY());
        // Centre the 80px image under the cursor (square is 100px, so offset by 50)
        dragging.setTranslateX(mouse.getX() - dragOriginX - 50);
        dragging.setTranslateY(mouse.getY() - dragOriginY - 50);
    }

    private void onDragEnd(MouseEvent event)
    {
        if (dragging == null || event.getButton() != MouseButton.PRIMARY) return;

        // Always snap the piece back to its layout position
        dragging.setTranslateX(0);
        dragging.setTranslateY(0);

        if (!isDragging)
        {
            // Mouse didn't move — treat as a plain click, already handled by MOUSE_PRESSED
            dragging   = null;
            dragSource = null;
            return;
        }

        isDragging = false;
        dragging   = null;

        // Determine which square the piece was dropped on from mouse position.
        // Board has 10px padding; each square is 100x100.
        Point2D mouse = this.sceneToLocal(event.getSceneX(), event.getSceneY());
        int col = (int)((mouse.getX() - 10) / 100);
        int row = (int)((mouse.getY() - 10) / 100);

        if (col < 0 || col > 7 || row < 0 || row > 7)
        {
            if (selectedSquare != null) setSelectedSquare(selectedSquare, false);
            dragSource = null;
            return;
        }

        BoardSquareView target = squaresMap.get(new Position(col, 7 - row));
        BoardSquareView source = dragSource;
        dragSource = null;

        if (target == null) return;

        if (target == source)
        {
            // Dropped back on source — deselect
            if (selectedSquare != null) setSelectedSquare(selectedSquare, false);
            return;
        }

        // Reuse the existing click logic to validate and execute the move
        processSelection(target);
    }

    private void clearBoard()
    {
        enPassantTarget      = null;
        gameOver             = false;
        halfMoveClock        = 0;
        awaitingPromotion    = false;
        pendingMoveNotation  = null;
        if (controlsPane != null) controlsPane.resetClock();
        if (moveHistoryPane != null) moveHistoryPane.clear();
        for(BoardSquareView square : squaresMap.values())
        {
            square.clearSquare();
        }
    }

    private String buildNotation(Piece type, Position from, Position to,
                                  boolean isCapture, boolean isEnPassant, Piece promotedTo)
    {
        StringBuilder sb = new StringBuilder();
        if (type == Piece.PAWN)
        {
            if (isCapture) sb.append(from.getFile()).append('x');
            sb.append(to.getFile()).append(to.getRank());
            if (promotedTo != null) sb.append('=').append(pieceChar(promotedTo));
            if (isEnPassant) sb.append(" e.p.");
        }
        else
        {
            sb.append(pieceChar(type));
            if (isCapture) sb.append('x');
            sb.append(to.getFile()).append(to.getRank());
        }
        return sb.toString();
    }

    private char pieceChar(Piece type)
    {
        return switch (type)
        {
            case KING   -> 'K';
            case QUEEN  -> 'Q';
            case ROOK   -> 'R';
            case BISHOP -> 'B';
            case KNIGHT -> 'N';
            default     -> '?';
        };
    }

}
