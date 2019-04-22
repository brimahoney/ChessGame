package network;

import java.net.Socket;

public class HeartBeatManager 
{
    private Socket heartBeatSocket;
    
    public HeartBeatManager(Socket socket)
    {
        this.heartBeatSocket = socket;
    }
}
