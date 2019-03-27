package chessgame;

public enum Piece
{
    PAWN ("Pawn", 1),
    KNIGHT ("Knight", 3.5),
    BISHOP ("Bishop", 3.5),
    ROOK ("Rook", 5),
    QUEEN ("Queen", 9),
    KING ("King", 1000);
    
    private String name;
    private double strength;
    
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
