
package chessgame;

import chessgame.pieces.Bishop;
import chessgame.pieces.ChessPiece;
import chessgame.pieces.King;
import chessgame.pieces.Knight;
import chessgame.pieces.Pawn;
import chessgame.pieces.Queen;
import chessgame.pieces.Rook;
import javafx.scene.image.Image;

public class Squad
{
    private King king;
    private Queen queen;
    private Bishop[] bishops = new Bishop[2];
    private Knight[] knights = new Knight[2];
    private Rook[] rooks = new Rook[2];
    private Pawn[] pawns = new Pawn[8];
    
    private ChessPiece[] pieces; 
    
    public Squad(TeamColor color)
    {
        pieces = new ChessPiece[16];
        createSquad(color);
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
        for(int j = 0; j < this.pawns.length; j++)
        {
            pieces[i++] = this.pawns[j];
        }
    }
    
    private void createKingAndQueen(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.KING, color);
        this.king = new King(new Position('e', rank), color, image);
        image = ImageLibrary.getImage(Piece.QUEEN, color);
        this.queen = new Queen(new Position('d', rank), color, image);
    }

    private void createBishops(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.BISHOP, color);
        bishops[0] = new Bishop(new Position('c', rank), color, image);
        bishops[1] = new Bishop(new Position('f', rank), color, image);
    }
    
    private void createKnights(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.KNIGHT, color);
        knights[0] = new Knight(new Position('b', rank), color, image);
        knights[1] = new Knight(new Position('g', rank), color, image);
    }
    
    private void createRooks(TeamColor color)
    {
        int rank = Squad.decideRank(color, false);
        Image image = ImageLibrary.getImage(Piece.ROOK, color);
        rooks[0] = new Rook(new Position('a', rank), color, image);
        rooks[1] = new Rook(new Position('h', rank), color, image);
    }
    
    private void createPawns(TeamColor color)
    {
        int rank = Squad.decideRank(color, true);
        char file = 'a';
        Image image = ImageLibrary.getImage(Piece.PAWN, color);
        
        for(int i = 0; i < pawns.length; i++)
        {
            pawns[i] = new Pawn(new Position(file++, rank), color, image);
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
