package chessgame.view;

import chessgame.model.TeamColor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Scrolling move history panel showing algebraic notation in two columns
 * (white / black), numbered by full move.
 */
public class MoveHistoryPane extends VBox
{
    private static final Color NUM_COLOR   = Color.web("#8899aa");
    private static final Color WHITE_COLOR = Color.web("#f0f0f0");
    private static final Color BLACK_COLOR = Color.web("#b8c8d8");

    private final GridPane grid;
    private final ScrollPane scroll;

    private int moveNumber   = 1;
    private int gridRow      = 0;
    private boolean halfRowPending = false; // true after white moves, awaiting black

    public MoveHistoryPane()
    {
        this.setStyle("-fx-background-color: #1e2a38;");
        this.setPrefWidth(210);
        this.setMinWidth(210);

        Label header = new Label("Move History");
        header.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 13));
        header.setTextFill(Color.web("#d4b483"));
        header.setPadding(new Insets(10, 12, 8, 12));
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-border-color: #0d151f; -fx-border-width: 0 0 1 0;");

        // Column headers
        GridPane colHeaders = new GridPane();
        colHeaders.setPadding(new Insets(5, 8, 5, 8));
        colHeaders.setStyle("-fx-background-color: #162030;"
                          + "-fx-border-color: #0d151f;"
                          + "-fx-border-width: 0 0 1 0;");

        ColumnConstraints hNumCol   = new ColumnConstraints(32);
        ColumnConstraints hWhiteCol = new ColumnConstraints(82);
        ColumnConstraints hBlackCol = new ColumnConstraints(82);
        colHeaders.getColumnConstraints().addAll(hNumCol, hWhiteCol, hBlackCol);

        Label hashHeader  = makeHeaderLabel("#");
        Label whiteHeader = makeHeaderLabel("White");
        Label blackHeader = makeHeaderLabel("Black");
        colHeaders.add(hashHeader,  0, 0);
        colHeaders.add(whiteHeader, 1, 0);
        colHeaders.add(blackHeader, 2, 0);

        grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(4, 8, 4, 8));

        ColumnConstraints numCol   = new ColumnConstraints(32);
        ColumnConstraints whiteCol = new ColumnConstraints(82);
        ColumnConstraints blackCol = new ColumnConstraints(82);
        grid.getColumnConstraints().addAll(numCol, whiteCol, blackCol);

        scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: #1e2a38; -fx-background-color: #1e2a38;"
                      + "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        this.getChildren().addAll(header, colHeaders, scroll);
    }

    /**
     * Appends a move to the history.
     * @param notation algebraic notation string (already includes +/# if applicable)
     * @param color    the color that made this move
     */
    public void addMove(String notation, TeamColor color)
    {
        if (color == TeamColor.WHITE)
        {
            grid.add(makeLabel(moveNumber + ".", NUM_COLOR),   0, gridRow);
            grid.add(makeLabel(notation,          WHITE_COLOR), 1, gridRow);
            gridRow++;
            halfRowPending = true;
        }
        else if (halfRowPending)
        {
            // Fill black's column on the last row
            grid.add(makeLabel(notation, BLACK_COLOR), 2, gridRow - 1);
            moveNumber++;
            halfRowPending = false;
        }
        else
        {
            // Black moves first (game started with black)
            grid.add(makeLabel(moveNumber + "...", NUM_COLOR),  0, gridRow);
            grid.add(makeLabel("",                 WHITE_COLOR), 1, gridRow);
            grid.add(makeLabel(notation,           BLACK_COLOR), 2, gridRow);
            gridRow++;
            moveNumber++;
        }

        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    public void clear()
    {
        grid.getChildren().clear();
        moveNumber   = 1;
        gridRow      = 0;
        halfRowPending = false;
    }

    private Label makeLabel(String text, Color color)
    {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 12));
        label.setTextFill(color);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(2, 4, 2, 2));
        return label;
    }

    private Label makeHeaderLabel(String text)
    {
        Label label = new Label(text);
        label.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#8899aa"));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(0, 4, 0, 2));
        return label;
    }
}
