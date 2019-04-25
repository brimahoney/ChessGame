package network;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeatManager
{
    private Socket heartBeatSocket;
    private boolean isStopped = false;
    private Thread sendThread;
    private Thread recieveThread;
    private ScheduledExecutorService heartBeatExecutor;
    
    public HeartBeatManager(Socket socket)
    {
        this.heartBeatSocket = socket;
        heartBeatExecutor = Executors.newScheduledThreadPool(2);
    }
    
    public void startHeartBeatSendAndRecieve()
    {
        long period = 5000L;
        heartBeatExecutor.scheduleAtFixedRate(getHeartBeatSenderTask(), period, period, TimeUnit.MILLISECONDS);
    }
    
    private synchronized boolean isStopped() 
    {
        return this.isStopped;
    }

    public synchronized void stop()
    {
        this.isStopped = true;
        this.heartBeatExecutor.shutdown();
        
        try 
        {
            this.heartBeatSocket.close();
        }
        catch (IOException e) 
        {
            throw new RuntimeException("Error closing heartbeat socket", e);
        }
    }
    
    private Runnable getHeartBeatSenderTask()
    {
        Runnable heartBeatTask = new Runnable() 
        {
            public void run() 
            {
                
                System.out.println("Task performed on " + new Date());
            }
        };
        
        return heartBeatTask;
    }
    
    private Runnable getHeartBeatReceieverTask()
    {
        Runnable heartBeatTask = new Runnable() 
        {
            public void run() 
            {
                
                System.out.println("Task performed on " + new Date());
            }
        };
        
        return heartBeatTask;
    }
}