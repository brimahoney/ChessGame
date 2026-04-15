package chessgame.view;

import chessgame.model.TeamColor;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ControlsPane extends StackPane
{
    private final HBox controlsBox;
    private final Button startButton;
    private final Label playAsLabel;
    private final ToggleGroup colorSelectGroup;
    private final RadioButton blackSelectRadio;
    private final RadioButton whiteSelectRadio;
    private final Label statusLabel;
    private final ChessBoard board;
    private final Circle turnCircle;
    private final Label turnLabel;

    public ControlsPane(ChessBoard board)
    {
        this.board = board;
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
        startButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                TeamColor startingColor = whiteSelectRadio.isSelected() ? TeamColor.WHITE : TeamColor.BLACK;
                board.startNewGame(startingColor);
            }
        });

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

        HBox radioBox = new HBox(6, playAsLabel, whiteSelectRadio, blackSelectRadio);
        radioBox.setAlignment(Pos.CENTER_LEFT);

        // Turn indicator: filled circle showing current player's color + text label
        turnCircle = new Circle(9, Color.WHITE);
        turnCircle.setStroke(Color.web("#888888"));
        turnCircle.setStrokeWidth(1.5);

        turnLabel = new Label("White's turn");
        turnLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 13));
        turnLabel.setTextFill(Color.web("#d0d8e0"));

        HBox turnIndicator = new HBox(8, turnCircle, turnLabel);
        turnIndicator.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("");
        statusLabel.setPrefSize(200, 20);
        statusLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.web("#e8c060"));

        controlsBox.getChildren().addAll(radioBox, startButton, turnIndicator, statusLabel);
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
    }
}
