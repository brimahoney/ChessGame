package network;

import chessgame.ChessMove;

/**
 *
 * @author brian.mahoney
 */
public class MoveMessage extends ChessMessage
{
    private ChessMove move;
    
    public MoveMessage()
    {
        super(MessageType.MOVE_MESSAGE);
    }

    @Override
    public void setPayload(Object payload) throws IllegalArgumentException
    {
        if(!(payload instanceof ChessMove))
            throw new IllegalArgumentException("Payload type of ChessMove expected");
        else
            this.move = (ChessMove)payload;
    }

    @Override
    public Object getPayload() 
    {
        return move;
    }
    
}
