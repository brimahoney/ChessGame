package chessgame;

import java.util.Objects;

public class Position
{
    private final int rank;
    private final char file;
    private final int x;
    private final int y;
    
    public Position(char file, int rank)
    {
        this.rank = rank;
        this.file = file;
        this.x = Position.translateRank(rank);
        this.y = Position.translateFile(file);
    }
    
    public Position(int x, int y)
    {
        this((char)(y + 97), x + 1);
    }

    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }

    public int getX()
    {
        return x;
    }

    /**
     * @return the file
     */
    public char getFile()
    {
        return file;
    }

    public int getY()
    {
        return y;
    }
        
    /**
     * Convert rank into int (x) index
     * @param rank
     * @return 
     */
    public static int translateRank(int rank)
    {
        return rank - 1;
    }
    
    /**
     * Convert file into int (y) index
     * @param file
     * @return 
     */
    public static int translateFile(char file)
    {
        int ascii = (int)file;
        return ascii - 97;
    }

    @Override
    public boolean equals(Object thePosition)
    {
        // If the object is compared with itself then return true   
        if (thePosition == this) 
        { 
            return true; 
        } 
  
        /* Check if thePosition is an instance of Position or not 
          "null instanceof [type]" also returns false */
        if (!(thePosition instanceof Position)) 
        { 
            return false; 
        } 
        
        Position p = (Position)thePosition;
        
        return this.getRank() == p.getRank() &&
                this.getFile() == p.getFile();
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(rank, file); 
    }
    
    public String toString()
    {
        return "Position: rank: " + rank + ", file: " + file;
    }
}
