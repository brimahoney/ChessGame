package network.messageProcesors;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class MessageProcessorRunnable implements Runnable
{

    protected Socket clientSocket = null;

    public MessageProcessorRunnable(Socket clientSocket) 
    {
        this.clientSocket = clientSocket;
    }

    public abstract void run(); 
}