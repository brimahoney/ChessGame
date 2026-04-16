package chessgame.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chessgame.model.Piece;
import chessgame.model.TeamColor;
import javafx.scene.image.Image;

public class ImageLibrary
{
    /**
     * Each entry in this list corresponds to a subfolder under src/images/.
     * To add a new icon set: create the folder, add 12 correctly-named PNGs
     * (e.g. White_Pawn.png, Black_King.png), then add the display name here.
     * Folder name = display name lowercased  (e.g. "Frogs" -> src/images/frogs/).
     */
    private static final List<String> ICON_SETS =
            Collections.unmodifiableList(Arrays.asList("Classic", "Frogs"));

    private static String currentIconSet = "Classic";
    private static final Map<String, Image> images = new HashMap<>();

    static
    {
        initImages("Classic");
    }

    private ImageLibrary() {}

    public static List<String> getAvailableIconSets()
    {
        return ICON_SETS;
    }

    public static String getCurrentIconSet()
    {
        return currentIconSet;
    }

    /**
     * Switches to the given icon set and reloads all piece images.
     * After calling this, all existing BoardSquareViews must call refreshPieceView()
     * to pick up the new images.
     */
    public static void loadIconSet(String setName)
    {
        currentIconSet = setName;
        initImages(setName);
    }

    /**
     * Loads images for setName into the cache without touching currentIconSet.
     * Kept private so the static initializer can call it without writing to
     * any non-final static fields.
     */
    private static void initImages(String setName)
    {
        images.clear();
        String folder = setName.toLowerCase();
        for (Piece p : Piece.values())
        {
            for (TeamColor c : TeamColor.values())
            {
                String imageName = getImageName(p, c);
                // Image image = new Image("images/" + imageName, 75, 75, false, false); // Original NetBeans path - doesn't work with Maven classpath
                Image image = new Image(ImageLibrary.class.getResource(
                        "/images/" + folder + "/" + imageName).toString());
                images.put(imageName, image);
            }
        }
    }

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
}
