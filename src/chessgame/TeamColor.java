package chessgame;

import java.io.Serializable;

public enum TeamColor implements Serializable
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
