package network;

public class HeartBeatMessage extends ChessMessage
{
    Object payload;
    
    public HeartBeatMessage() 
    {
        super(MessageType.HEARTBEAT_MESSAGE);
    }

    @Override
    public void setPayload(Object payload) 
    {
        this.payload = payload;
    }

    @Override
    public Object getPayload() 
    {
        return this.payload;
    }
}
