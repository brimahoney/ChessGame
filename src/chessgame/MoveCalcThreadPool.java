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
    private final BoardSquare[][] board;
    private List<MovesCalculator> blackTeamTasks = new ArrayList<>();
    private List<MovesCalculator> whiteTeamTasks = new ArrayList<>();
                
    public MoveCalcThreadPool(BoardSquare[][] board, Squad whiteSquad, Squad blackSquad) 
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
        createTasks(whiteSquad);
        createTasks(blackSquad);         
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
        List<MovesCalculator> tasks = squad.getColor().equals(TeamColor.BLACK) ? blackTeamTasks : whiteTeamTasks;
        return this.pool.invokeAll(tasks);
    }

    void shutDown() 
    {
        this.pool.shutdown();
    }
}


