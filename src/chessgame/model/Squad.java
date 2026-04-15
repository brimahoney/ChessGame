package chessgame.model;

public class Squad
{
    private final TeamColor color;
    private final ChessPiece[] pieces;

    public Squad(TeamColor color)
    {
        this.color = color;
        this.pieces = createPieces(color);
    }

    public TeamColor getColor()
    {
        return color;
    }

    public ChessPiece[] getSquad()
    {
        return pieces;
    }

    private static ChessPiece[] createPieces(TeamColor color)
    {
        int back = color == TeamColor.WHITE ? 1 : 8;
        int pawn = color == TeamColor.WHITE ? 2 : 7;

        return new ChessPiece[] {
            new ChessPiece(new Position('e', back), color, Piece.KING),
            new ChessPiece(new Position('d', back), color, Piece.QUEEN),
            new ChessPiece(new Position('c', back), color, Piece.BISHOP),
            new ChessPiece(new Position('f', back), color, Piece.BISHOP),
            new ChessPiece(new Position('b', back), color, Piece.KNIGHT),
            new ChessPiece(new Position('g', back), color, Piece.KNIGHT),
            new ChessPiece(new Position('a', back), color, Piece.ROOK),
            new ChessPiece(new Position('h', back), color, Piece.ROOK),
            new ChessPiece(new Position('a', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('b', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('c', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('d', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('e', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('f', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('g', pawn),  color, Piece.PAWN),
            new ChessPiece(new Position('h', pawn),  color, Piece.PAWN),
        };
    }
}
