package chessgame;

import chessgame.pieces.ChessPiece;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

public class ImageLibrary
{
    private static Map<String, Image> images = new HashMap<>();
    static
    {
        createImages();
    }
    
    private ImageLibrary(){}
    
    public static String getImageName(Piece piece, TeamColor color)
    {
        return color.getColorName() + "_" + piece.getName() + ".png";
    }
    
    public static Image getImage(String imageName)
    {
        return images.get(imageName);
    }
    
    public static Image getImage(Piece piece, TeamColor color)
    {
        return getImage(getImageName(piece, color));
    }
    
    private static void createImages()
    {
        for (Piece p : Piece.values()) 
        {
            for(TeamColor c : TeamColor.values())
            {
                String imageName = ImageLibrary.getImageName(p, c);
                Image image = new Image("images/" + imageName, 75, 75, false, false);
                images.put(imageName, image);
            }
        }
    }
}
