package chessgame;

public enum TeamColor
{
    BLACK("Black"), WHITE("White");
    
    private final String name;
    
    TeamColor(String name)
    {
        this.name = name;
    }
    
    public String getColorName()
    {
        return name;
    }
}
