package chessgame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoveCalcThreadPool
{
    private final ExecutorService pool;
            
    public MoveCalcThreadPool(BoardSquare[][] board) 
    {
        this.pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        Runtime.getRuntime().addShutdownHook(new Thread() 
        { 
            @SuppressWarnings("override")
            public void run() 
            { 
                System.out.println("Shutdown Hook is running !"); 
                pool.shutdown();
            } 
        }); 
        System.out.println("Application Terminating ..."); 
    }
}


