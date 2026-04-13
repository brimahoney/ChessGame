
package chessgame;

import chessgame.pieces.ChessPiece;
import chessgame.pieces.Piece;
import javafx.scene.image.Image;

public class Squad
{
    private ChessPiece king;
    private ChessPiece queen;
    private ChessPiece[] bishops = new ChessPiece[2];
    private ChessPiece[] knights = new ChessPiece[2];
    private ChessPiece[] rooks = new ChessPiece[2];
    private ChessPiece[] pawns = new ChessPiece[8];

    private final TeamColor color;
    
    private ChessPiece[] pieces; 
    
    public Squad(TeamColor color)
    {
        this.color = color;
        pieces = new ChessPiece[16];
        createSquad(color);
    }
    
    public TeamColor getColor()
    {
        return this.color;
    }
    
    public ChessPiece[] getSquad()
    {
        return pieces;
    }
    
    private void createSquad(TeamColor color)
    {
        int i = 0;
        createKingAndQueen(color);
        pieces[i++] = this.king;
        pieces[i++] = this.queen;
        createBishops(color);
        pieces[i++] = this.bishops[0];
        pieces[i++] = this.bishops[1];
        createKnights(color);
        pieces[i++] = this.knights[0];
        pieces[i++] = this.knights[1];
        createRooks(color);
        pieces[i++] = this.rooks[0];
        pieces[i++] = this.rooks[1];
        createPawns(color);
        for (ChessPiece pawn : this.pawns)
        {
            pieces[i++] = pawn;
        }
    }
    
    private void createKingAndQueen(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.KING, color);
        this.king = new ChessPiece(new Position('e', rank), color, image, Piece.KING);
        image = ImageLibrary.getImage(Piece.QUEEN, color);
        this.queen = new ChessPiece(new Position('d', rank), color, image, Piece.QUEEN);
    }

    private void createBishops(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.BISHOP, color);
        bishops[0] = new ChessPiece(new Position('c', rank), color, image, Piece.BISHOP);
        bishops[1] = new ChessPiece(new Position('f', rank), color, image, Piece.BISHOP);
    }
    
    private void createKnights(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.KNIGHT, color);
        knights[0] = new ChessPiece(new Position('b', rank), color, image, Piece.KNIGHT);
        knights[1] = new ChessPiece(new Position('g', rank), color, image, Piece.KNIGHT);
    }
    
    private void createRooks(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.ROOK, color);
        rooks[0] = new ChessPiece(new Position('a', rank), color, image, Piece.ROOK);
        rooks[1] = new ChessPiece(new Position('h', rank), color, image, Piece.ROOK);
    }
    
    private void createPawns(TeamColor color)
    {
        int rank = Squad.decideRank(color, true);
        char file = 'a';
        Image image = ImageLibrary.getImage(Piece.PAWN, color);
        
        for(int i = 0; i < pawns.length; i++)
        {
            pawns[i] = new ChessPiece(new Position(file++, rank), color, image, Piece.PAWN);
        }
    }
    
    public void promotePawn()
    {
        // promote the pawn to queen, rook, bishop, or knight
    }
    
    public static int decideRank(TeamColor color, boolean isPawn)
    {
        int rank;
        if(color == TeamColor.WHITE)
        {
            rank = isPawn ? 2 : 1;
        }
        else
        {
            rank = isPawn ? 7 : 8;
        }
        return rank;
    }
}
