package chessgame.view;

import chessgame.model.TeamColor;
import chessgame.network.NetworkManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChessGame extends Application
{
    private final ChessBoard chessBoard;
    private final ControlsPane controlsPane;
    private final BorderPane mainLayout;
    private final Label warningLabel;
    private final PromotionPane promotionPane;
    private final MoveHistoryPane moveHistoryPane;
    private final NetworkManager networkManager;
    
    public ChessGame()
    {
        chessBoard = new ChessBoard();
        controlsPane = new ControlsPane(chessBoard);
        chessBoard.setControlsPane(controlsPane);
        mainLayout = new BorderPane();

        warningLabel = new Label();
        warningLabel.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 36));
        warningLabel.setTextFill(Color.WHITE);
        warningLabel.setStyle("-fx-background-color: rgba(0,0,0,0.65); -fx-padding: 12 24 12 24; -fx-background-radius: 8;");
        warningLabel.setMouseTransparent(true);
        warningLabel.setVisible(false);
        chessBoard.setWarningLabel(warningLabel);

        promotionPane = new PromotionPane();
        chessBoard.setPromotionPane(promotionPane);

        moveHistoryPane = new MoveHistoryPane();
        chessBoard.setMoveHistoryPane(moveHistoryPane);

        networkManager = new NetworkManager();
    }
    
    @Override
    public void start(Stage primaryStage)
    {
        StackPane centerPane = new StackPane(chessBoard, warningLabel, promotionPane);
        centerPane.setStyle("-fx-background-color: #3d2b1f;");

        mainLayout.setTop(controlsPane);
        mainLayout.setCenter(centerPane);
        mainLayout.setRight(moveHistoryPane);
        mainLayout.setStyle("-fx-background-color: #3d2b1f;");
        Scene scene = new Scene(mainLayout); //, 800, 800 + controlsPane.getHeight());

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Keep the board square as the window resizes
        Runnable fitBoard = () -> {
            double availW = mainLayout.getWidth()  - moveHistoryPane.getWidth();
            double availH = mainLayout.getHeight() - controlsPane.getHeight();
            double size   = Math.max(0, Math.min(availW, availH));
            chessBoard.setPrefSize(size, size);
        };
        mainLayout.widthProperty().addListener((obs, o, n)  -> fitBoard.run());
        mainLayout.heightProperty().addListener((obs, o, n) -> fitBoard.run());
        fitBoard.run();

        chessBoard.startGame(TeamColor.WHITE);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
   
    @Override
    public void stop() {}

}
