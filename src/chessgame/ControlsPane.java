package chessgame;



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
    
    public ControlsPane()
    {
        controlsBox = new HBox();
        controlsBox.setPadding(new Insets(15, 12, 20, 12));
        controlsBox.setSpacing(10);
        controlsBox.setStyle("-fx-background-color: #336699;");

        startButton = new Button("Start a new Game");
        startButton.setPrefSize(100, 20);
        startButton.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, USE_PREF_SIZE));
        startButton.setTextFill(Color.RED);

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
        
        controlsBox.getChildren().addAll(radioBox, startButton);
        this.getChildren().addAll(controlsBox);
    }
}
