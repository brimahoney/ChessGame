package network;

import java.io.Serializable;

public abstract class ChessMessage implements Serializable
{
    private final MessageType messageType;
    
    public ChessMessage(MessageType type)
    {
        this.messageType = type;
    }
    
    public abstract void setPayload(Object payload) throws IllegalArgumentException;
    public abstract Object getPayload();
}
