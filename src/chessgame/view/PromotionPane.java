package chessgame.view;

import chessgame.model.Piece;
import chessgame.model.TeamColor;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Semi-transparent overlay that lets the active player pick a promotion piece.
 * Call show() to populate for the correct color and make it visible;
 * it hides itself after the player clicks a piece.
 */
public class PromotionPane extends VBox
{
    private static final Piece[] CHOICES = {Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT};

    public PromotionPane()
    {
        this.setVisible(false);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(12);
        this.setStyle("-fx-background-color: #2c1e10;"
                    + "-fx-border-color: #c8a96e;"
                    + "-fx-border-width: 2;"
                    + "-fx-border-radius: 12;"
                    + "-fx-background-radius: 12;"
                    + "-fx-padding: 20 30 20 30;");
        this.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        this.setMouseTransparent(false);
    }

    /**
     * Populates the picker for the given color and shows it.
     * @param color    the promoting player's color (determines which piece images to show)
     * @param onSelect called with the chosen Piece type once the player clicks
     */
    public void show(TeamColor color, Consumer<Piece> onSelect)
    {
        this.getChildren().clear();

        Label title = new Label("Promote your pawn");
        title.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#d4b483"));

        HBox pieceRow = new HBox(8);
        pieceRow.setAlignment(Pos.CENTER);

        for (Piece type : CHOICES)
        {
            ImageView iv = new ImageView(ImageLibrary.getImage(type, color));
            iv.setFitWidth(80);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);

            StackPane btn = new StackPane(iv);
            btn.setPrefSize(100, 100);
            btn.setStyle(idleStyle());
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle()));
            btn.setOnMouseExited(e  -> btn.setStyle(idleStyle()));
            btn.setOnMouseClicked(e -> {
                this.setVisible(false);
                onSelect.accept(type);
            });

            pieceRow.getChildren().add(btn);
        }

        this.getChildren().addAll(title, pieceRow);
        this.setVisible(true);
    }

    private static String idleStyle()
    {
        return "-fx-background-color: #3d2b1f;"
             + "-fx-background-radius: 6; -fx-cursor: hand;";
    }

    private static String hoverStyle()
    {
        return "-fx-background-color: #6b3f1f;"
             + "-fx-background-radius: 6; -fx-cursor: hand;";
    }
}
