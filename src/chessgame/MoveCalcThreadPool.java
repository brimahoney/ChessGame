package chessgame;

import chessgame.pieces.ChessPiece;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MoveCalcThreadPool
{
    private final ExecutorService pool;
    private BoardSquare[][] board;
    private List<MovesCalculator> blackTeamTasks = new ArrayList<>();
    private List<MovesCalculator> whiteTeamTasks = new ArrayList<>();
                
    public MoveCalcThreadPool(BoardSquare[][] board) 
    {
        this.board = board;
        
        this.pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        Runtime.getRuntime().addShutdownHook(new Thread() 
        { 
            @SuppressWarnings("override")
            public void run() 
            { 
                System.out.println("Shutdown Hook is running !"); 
                pool.shutdown();
                
                System.out.println("Application Terminating ..."); 
            } 
        }); 
    }
    
    public void createTasks(Squad squad)
    {
        ChessPiece[] pieces = squad.getSquad();
        List<MovesCalculator> tasks = new ArrayList<>();
        TeamColor color = squad.getColor();
        
        for(ChessPiece piece : pieces)
        {
            MovesCalculator moveCalc = new MovesCalculator(piece, this.board);
            tasks.add(moveCalc);
        }
        if(color.equals(TeamColor.BLACK ))
            blackTeamTasks = tasks;
        else
            whiteTeamTasks = tasks;
    }
    
    public List<Future<Set<Position>>> calculateMoves(Squad squad) throws InterruptedException
    {
        createTasks(squad);
        List<MovesCalculator> tasks;
        if(squad.getColor().equals(TeamColor.BLACK))
            tasks = blackTeamTasks;
        else
            tasks = whiteTeamTasks;
        
        return this.pool.invokeAll(tasks);
    }

    void shutDown() 
    {
        this.pool.shutdown();
    }
}


