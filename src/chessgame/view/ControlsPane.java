package chessgame.view;

import chessgame.engine.ChessAI.Difficulty;
import chessgame.model.TeamColor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class ControlsPane extends StackPane
{
    private static final Color ACTIVE_TIME   = Color.web("#f0f0f0");
    private static final Color INACTIVE_TIME = Color.web("#505a66");

    private final HBox controlsBox;
    private final Button startButton;
    private final Label playAsLabel;
    private final ToggleGroup colorSelectGroup;
    private final RadioButton blackSelectRadio;
    private final RadioButton whiteSelectRadio;
    private final Label statusLabel;
    private final Circle turnCircle;
    private final Label turnLabel;

    // Per-side game timers
    private final Label whiteTimeLabel;
    private final Label blackTimeLabel;
    private final Timeline clockTimeline;
    private TeamColor activeColor = TeamColor.WHITE;
    private int whiteTotalSeconds = 0;
    private int blackTotalSeconds = 0;

    public ControlsPane(ChessBoard board)
    {
        controlsBox = new HBox();
        controlsBox.setPadding(new Insets(10, 16, 10, 16));
        controlsBox.setSpacing(14);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.setStyle("-fx-background-color: #1e2a38; -fx-border-color: #0d151f; -fx-border-width: 0 0 2 0;");

        startButton = new Button("New Game");
        startButton.setPrefSize(90, 28);
        startButton.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        startButton.setTextFill(Color.web("#1e2a38"));
        startButton.setStyle("-fx-background-color: #c8a96e; -fx-background-radius: 4;");
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #d4b87a; -fx-background-radius: 4; -fx-cursor: hand;"));
        startButton.setOnMouseExited(e  -> startButton.setStyle("-fx-background-color: #c8a96e; -fx-background-radius: 4;"));
        startButton.setOnMousePressed(e -> startButton.setStyle("-fx-background-color: #a8894e; -fx-background-radius: 4;"));
        startButton.setOnMouseReleased(e -> startButton.setStyle("-fx-background-color: #d4b87a; -fx-background-radius: 4; -fx-cursor: hand;"));

        playAsLabel = new Label("Play as:");
        playAsLabel.setPrefSize(55, 20);
        playAsLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        playAsLabel.setTextFill(Color.web("#a0b0c0"));

        colorSelectGroup = new ToggleGroup();

        whiteSelectRadio = new RadioButton("White");
        whiteSelectRadio.setToggleGroup(colorSelectGroup);
        whiteSelectRadio.setPrefSize(60, 20);
        whiteSelectRadio.setSelected(true);
        whiteSelectRadio.setTextFill(Color.web("#d0d8e0"));

        blackSelectRadio = new RadioButton("Black");
        blackSelectRadio.setToggleGroup(colorSelectGroup);
        blackSelectRadio.setPrefSize(60, 20);
        blackSelectRadio.setTextFill(Color.web("#d0d8e0"));

        // Opponent / difficulty selector
        Label opponentLabel = new Label("vs:");
        opponentLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        opponentLabel.setTextFill(Color.web("#a0b0c0"));

        ComboBox<String> opponentCombo = new ComboBox<>();
        opponentCombo.getItems().addAll("Human", "Easy AI", "Hard AI", "Expert AI");
        opponentCombo.setValue("Human");
        opponentCombo.setPrefHeight(28);
        opponentCombo.setStyle(
                "-fx-background-color: #2e3d50; -fx-border-color: #4a6080; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;");
        opponentCombo.setButtonCell(new ListCell<>()
        {
            @Override protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.web("#d0d8e0"));
                setStyle("-fx-background-color: transparent;");
            }
        });

        startButton.setOnAction(e -> {
            TeamColor playerColor = whiteSelectRadio.isSelected() ? TeamColor.WHITE : TeamColor.BLACK;
            TeamColor aiColor     = playerColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
            switch (opponentCombo.getValue())
            {
                case "Easy AI"   -> board.setAI(aiColor, Difficulty.EASY);
                case "Hard AI"   -> board.setAI(aiColor, Difficulty.HARD);
                case "Expert AI" -> board.setAI(aiColor, Difficulty.EXPERT);
                default          -> board.setAI(null, Difficulty.HARD);
            }
            board.startNewGame(playerColor);
        });

        HBox opponentBox = new HBox(6, opponentLabel, opponentCombo);
        opponentBox.setAlignment(Pos.CENTER_LEFT);

        HBox radioBox = new HBox(6, playAsLabel, whiteSelectRadio, blackSelectRadio);
        radioBox.setAlignment(Pos.CENTER_LEFT);

        // Turn indicator
        turnCircle = new Circle(9, Color.WHITE);
        turnCircle.setStroke(Color.web("#888888"));
        turnCircle.setStrokeWidth(1.5);

        turnLabel = new Label("White's turn");
        turnLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 13));
        turnLabel.setTextFill(Color.web("#d0d8e0"));

        HBox turnIndicator = new HBox(8, turnCircle, turnLabel);
        turnIndicator.setAlignment(Pos.CENTER_LEFT);

        // Per-side timers
        whiteTimeLabel = makeTimeLabel("0:00", ACTIVE_TIME);
        blackTimeLabel = makeTimeLabel("0:00", INACTIVE_TIME);

        Circle whiteCircle = new Circle(6, Color.WHITE);
        whiteCircle.setStroke(Color.web("#888888"));
        whiteCircle.setStrokeWidth(1);

        Circle blackCircle = new Circle(6, Color.BLACK);
        blackCircle.setStroke(Color.web("#aaaaaa"));
        blackCircle.setStrokeWidth(1);

        HBox whiteTimer = new HBox(5, whiteCircle, whiteTimeLabel);
        whiteTimer.setAlignment(Pos.CENTER_LEFT);

        HBox blackTimer = new HBox(5, blackCircle, blackTimeLabel);
        blackTimer.setAlignment(Pos.CENTER_LEFT);

        HBox timerBox = new HBox(12, whiteTimer, blackTimer);
        timerBox.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("");
        statusLabel.setPrefSize(200, 20);
        statusLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.web("#e8c060"));

        // Clock — ticks every second, increments the active side's counter
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (activeColor == TeamColor.WHITE)
                whiteTotalSeconds++;
            else
                blackTotalSeconds++;
            updateTimerLabels();
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);

        // Icon set selector
        Label piecesLabel = new Label("Pieces:");
        piecesLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        piecesLabel.setTextFill(Color.web("#a0b0c0"));

        ComboBox<String> iconSetCombo = new ComboBox<>();
        iconSetCombo.getItems().addAll(ImageLibrary.getAvailableIconSets());
        iconSetCombo.setValue(ImageLibrary.getCurrentIconSet());
        iconSetCombo.setPrefHeight(28);
        iconSetCombo.setStyle(
                "-fx-background-color: #2e3d50; -fx-border-color: #4a6080; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;");
        iconSetCombo.setButtonCell(new ListCell<>()
        {
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.web("#d0d8e0"));
                setStyle("-fx-background-color: transparent;");
            }
        });
        iconSetCombo.setOnAction(e -> {
            String selected = iconSetCombo.getValue();
            if (selected != null && !selected.equals(ImageLibrary.getCurrentIconSet()))
            {
                ImageLibrary.loadIconSet(selected);
                board.refreshPieces();
            }
        });

        HBox iconSetBox = new HBox(6, piecesLabel, iconSetCombo);
        iconSetBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controlsBox.getChildren().addAll(turnIndicator, timerBox, statusLabel, spacer, opponentBox, iconSetBox, radioBox, startButton);
        this.getChildren().add(controlsBox);
    }

    public void setStatus(String message)
    {
        statusLabel.setText(message);
    }

    public void setTurn(TeamColor color)
    {
        boolean isWhite = color == TeamColor.WHITE;
        turnCircle.setFill(isWhite ? Color.WHITE : Color.BLACK);
        turnCircle.setStroke(isWhite ? Color.web("#888888") : Color.web("#aaaaaa"));
        turnLabel.setText(color.getColorName() + "'s turn");

        activeColor = color;
        whiteTimeLabel.setTextFill(isWhite ? ACTIVE_TIME : INACTIVE_TIME);
        blackTimeLabel.setTextFill(isWhite ? INACTIVE_TIME : ACTIVE_TIME);

        if (clockTimeline.getStatus() != Animation.Status.RUNNING)
            clockTimeline.play();
    }

    public void stopClock()
    {
        clockTimeline.stop();
    }

    public void resetClock()
    {
        clockTimeline.stop();
        whiteTotalSeconds = 0;
        blackTotalSeconds = 0;
        whiteTimeLabel.setText("0:00");
        blackTimeLabel.setText("0:00");
    }

    private void updateTimerLabels()
    {
        whiteTimeLabel.setText(formatTime(whiteTotalSeconds));
        blackTimeLabel.setText(formatTime(blackTotalSeconds));
    }

    private static String formatTime(int totalSeconds)
    {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private static Label makeTimeLabel(String text, Color color)
    {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        label.setTextFill(color);
        label.setMinWidth(38);
        return label;
    }
}
