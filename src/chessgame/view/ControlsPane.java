package chessgame.view;

import chessgame.model.TeamColor;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
    
    public ControlsPane(ChessBoard board)
    {
        this.board = board;
        controlsBox = new HBox();
        controlsBox.setPadding(new Insets(15, 12, 20, 12));
        controlsBox.setSpacing(10);
        controlsBox.setStyle("-fx-background-color: #336699;");

        startButton = new Button("Start a new Game");
        startButton.setPrefSize(100, 20);
        startButton.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, USE_PREF_SIZE));
        startButton.setTextFill(Color.RED);
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
        playAsLabel.setPrefSize(50, 20);
        playAsLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, USE_PREF_SIZE));
        
        colorSelectGroup = new ToggleGroup();

        whiteSelectRadio = new RadioButton("White");
        whiteSelectRadio.setToggleGroup(colorSelectGroup);
        whiteSelectRadio.setPrefSize(60, 20);
        whiteSelectRadio.setSelected(true);
        
        blackSelectRadio = new RadioButton("Black");
        blackSelectRadio.setToggleGroup(colorSelectGroup);
        blackSelectRadio.setPrefSize(60, 20);
      
        HBox radioBox = new HBox(playAsLabel, whiteSelectRadio, blackSelectRadio);
        radioBox.setPadding(new Insets(0, 12, 0, 12));
        //radioBox.setSpacing(10);
        
        statusLabel = new Label("");
        statusLabel.setPrefSize(200, 20);
        statusLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.YELLOW);

        controlsBox.getChildren().addAll(radioBox, startButton, statusLabel);
        this.getChildren().addAll(controlsBox);
    }

    public void setStatus(String message)
    {
        statusLabel.setText(message);
    }
}
