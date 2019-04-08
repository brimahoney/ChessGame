package chessgame;


import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChessGame extends Application
{
    private final ChessBoard chessBoard;
    private final ControlsPane controlsPane;
    private final BorderPane mainLayout;
    
    public ChessGame()
    {
        chessBoard = new ChessBoard();
        controlsPane = new ControlsPane(chessBoard);
        mainLayout = new BorderPane();
        //gridPane.setPadding(new Insets(10, 10, 10, 10));
        //gridPane.setAlignment(Pos.CENTER); 
        //gridPane.setBorder(new Border(new BorderStroke(Paint.valueOf("BLACK"), 
          //  BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        //gridPane.getTransforms().add(new Rotate(180, 400, 400));
    }
    
    @Override
    public void start(Stage primaryStage)
    {
        mainLayout.setTop(controlsPane);
        mainLayout.setCenter(chessBoard);
        Scene scene = new Scene(mainLayout); //, 800, 800 + controlsPane.getHeight());

        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
        
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
    public void stop()
    {
        chessBoard.shutdownMovesCalculator();
    }
}
