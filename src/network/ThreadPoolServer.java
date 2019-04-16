package network;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import network.messageProcesors.MessageProcessorRunnable;

public class ThreadPoolServer implements Runnable{

    protected int serverPort = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public ThreadPoolServer(int port)
    {
        this.serverPort = port;
    }

    public void run()
    {
        openServerSocket();
        
        while(!isStopped())
        {
            Socket clientSocket = null;
            
            try 
            {
                clientSocket = this.serverSocket.accept();
            }
            catch (IOException e) 
            {
                if(isStopped()) 
                {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            
            //this.threadPool.execute(new MessageProcessorRunnable(clientSocket));
        }
        
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() 
    {
        return this.isStopped;
    }

    public synchronized void stop()
    {
        this.isStopped = true;
        try 
        {
            this.serverSocket.close();
        }
        catch (IOException e) 
        {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() 
    {
        try 
        {
            this.serverSocket = new ServerSocket(this.serverPort);
        }
        catch (IOException e) 
        {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }
}