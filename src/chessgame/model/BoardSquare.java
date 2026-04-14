package chessgame.model;

/**
 * Pure model representation of a board square — no JavaFX dependencies.
 * Tracks occupancy and the piece currently on this square.
 * Visual state (selection highlight, move dots) lives in BoardSquareView.
 */
public class BoardSquare
{
    private final Position position;
    private final TeamColor color;
    private boolean isOccupied = false;
    private ChessPiece currentPiece;

    public BoardSquare(TeamColor color, Position position)
    {
        this.color = color;
        this.position = position;
    }

    public Position getPosition()
    {
        return position;
    }

    public TeamColor getColor()
    {
        return color;
    }

    public ChessPiece getCurrentPiece()
    {
        return currentPiece;
    }

    public void setCurrentPiece(ChessPiece piece)
    {
        if (piece != null)
        {
            if (currentPiece != null)
                currentPiece.setIsAlive(false);
            piece.setPosition(this.position);
            this.currentPiece = piece;
            isOccupied = true;
        }
        else
        {
            this.currentPiece = null;
            isOccupied = false;
        }
    }

    public boolean isOccupied()
    {
        return isOccupied;
    }

    public void clearSquare()
    {
        setCurrentPiece(null);
    }

    @Override
    public String toString()
    {
        return "File: " + position.getFile() + " Rank: " + position.getRank() +
                " Color: " + color.getColorName();
    }
}
