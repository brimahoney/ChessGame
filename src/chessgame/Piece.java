package chessgame;

import java.io.Serializable;

public enum Piece implements Serializable
{
    PAWN ("Pawn", 1),
    KNIGHT ("Knight", 3.5),
    BISHOP ("Bishop", 3.5),
    ROOK ("Rook", 5),
    QUEEN ("Queen", 9),
    KING ("King", 1000);
    
    private final String name;
    private final double strength;
    
    Piece(String name, double strength)
    {
        this.name = name;
        this.strength = strength;
    }
    
    public String getName()
    {
        return name;
    }

    public double getStrength()
    {
        return strength;
    }
}
