package chessgame.model;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable
{
    private final int x;
    private final int y;

    public Position(char file, int rank)
    {
        this.x = file - 'a';
        this.y = rank - 1;
    }

    public Position(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public char getFile()
    {
        return (char)(x + 'a');
    }

    public int getRank()
    {
        return y + 1;
    }

    @Override
    public boolean equals(Object thePosition)
    {
        if (thePosition == this) return true;
        if (!(thePosition instanceof Position)) return false;
        Position p = (Position) thePosition;
        return this.x == p.x && this.y == p.y;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    @Override
    public String toString()
    {
        return "Position: rank - " + getRank() + ", file - " + getFile() +
                "\n\t y - " + y + ", x - " + x;
    }
}
