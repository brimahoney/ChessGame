package chessgame.view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import chessgame.model.Piece;
import chessgame.model.TeamColor;
import javafx.scene.image.Image;

public class ImageLibrary
{
    /**
     * Maps display name → actual folder name on the classpath.
     * e.g. "Classic" → "classic",  "Frogs" → "Frogs"
     * Populated once at class load by scanning /images/ subdirectories.
     */
    private static final Map<String, String> SET_FOLDERS = new LinkedHashMap<>();

    /** Ordered list of display names for the icon-set selector UI. */
    private static final List<String> ICON_SETS;

    private static String currentIconSet;
    private static final Map<String, Image> images = new HashMap<>();

    static
    {
        discoverSets();
        ICON_SETS = Collections.unmodifiableList(new ArrayList<>(SET_FOLDERS.keySet()));
        currentIconSet = ICON_SETS.isEmpty() ? "" : ICON_SETS.get(0);
        if (!currentIconSet.isEmpty()) initImages(currentIconSet);
    }

    private ImageLibrary() {}

    // -------------------------------------------------------------------------

    public static List<String> getAvailableIconSets()
    {
        return ICON_SETS;
    }

    public static String getCurrentIconSet()
    {
        return currentIconSet;
    }

    /**
     * Switches to the given icon set (display name) and reloads all piece images.
     * After calling this, call board.refreshPieces() to redraw the pieces.
     */
    public static void loadIconSet(String displayName)
    {
        currentIconSet = displayName;
        initImages(displayName);
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

    // -------------------------------------------------------------------------

    /**
     * Scans the /images/ classpath directory for subdirectories and populates
     * SET_FOLDERS. Display name = first letter capitalised, rest as-is.
     * Sorted alphabetically so the order in the UI is predictable.
     */
    private static void discoverSets()
    {
        URL url = ImageLibrary.class.getResource("/images/");
        if (url == null || !url.getProtocol().equals("file")) return;

        try
        {
            File[] subdirs = new File(url.toURI()).listFiles(File::isDirectory);
            if (subdirs == null) return;

            List<String> folderNames = new ArrayList<>();
            for (File d : subdirs) folderNames.add(d.getName());
            Collections.sort(folderNames, String.CASE_INSENSITIVE_ORDER);

            for (String folder : folderNames)
            {
                String display = folder.isEmpty() ? folder
                        : Character.toUpperCase(folder.charAt(0)) + folder.substring(1);
                SET_FOLDERS.put(display, folder);
            }
        }
        catch (Exception e)
        {
            System.err.println("ImageLibrary: could not scan /images/ — " + e.getMessage());
        }
    }

    /**
     * Loads images for the given display name into the cache.
     * Uses the actual folder name from SET_FOLDERS to avoid case-sensitivity issues.
     */
    private static void initImages(String displayName)
    {
        String folder = SET_FOLDERS.getOrDefault(displayName, displayName.toLowerCase());
        images.clear();
        for (Piece p : Piece.values())
        {
            for (TeamColor c : TeamColor.values())
            {
                String imageName = getImageName(p, c);
                // Image image = new Image("images/" + imageName, 75, 75, false, false); // Original NetBeans path - doesn't work with Maven classpath
                URL resource = ImageLibrary.class.getResource("/images/" + folder + "/" + imageName);
                if (resource == null)
                {
                    System.err.println("ImageLibrary: missing " + folder + "/" + imageName);
                    continue;
                }
                images.put(imageName, new Image(resource.toString()));
            }
        }
    }
}
