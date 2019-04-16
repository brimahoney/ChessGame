package network.messageProcesors;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MoveProcessorRunnable extends MessageProcessorRunnable
{
    public MoveProcessorRunnable(Socket clientSocket)
    {
        super(clientSocket);
    }
    
    public void run() 
    {
        try 
        {
            OutputStream output = clientSocket.getOutputStream();

            output.close();
            System.out.println("Heartbeat Request processed");
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }   
    }    
}
