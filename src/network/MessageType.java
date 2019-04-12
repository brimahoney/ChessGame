package network;

import java.io.Serializable;

public enum MessageType implements Serializable
{
    GAME_REQUEST (1),
    START_NEW_GAME_REQUEST (2),
    SURRENDER_MESSAGE (3),
    SURRENDER_AND_NEW_GAME_REQUEST (4),
    MOVE_MESSAGE (5),
    HEARTBEAT_MESSAGE (6);
    
    private final int ID;
    
    MessageType(int ID)
    {
        this.ID = ID;
    }
    
    public int getID()
    {
        return ID;
    }
}
