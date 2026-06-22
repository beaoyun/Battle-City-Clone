package battlecity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public final class Sprites {

    private static final int S = 16; // One sprite cell/lenght in the "Battle City - Miscellaneous - General Sprites.png"

    private static BufferedImage sheet;
    private static BufferedImage miscSheet;

    static {
        sheet = tryLoad("Battle City - Miscellaneous - General Sprites.png");
        miscSheet = tryLoad("Battle City - Miscellaneous - Miscellaneous.png");
    }

    private static BufferedImage tryLoad(String name) {
        String[] paths = { name, "../" + name, "BattleCity/" + name };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try { return ImageIO.read(f); }
                catch (IOException ex) {
                    System.err.println("Could not read sprite: " + p);
                }
            }
        }
        System.err.println("Sprite sheet not found: " + name);
        return null;
    }

    private Sprites() {}

    public static boolean isLoaded() {return sheet != null;}

    // Crop a rectangle from the general sprite sheet
    static BufferedImage crop(int x, int y, int w, int h) {
        if (sheet == null) return null;
        if (x < 0 || y < 0 || x + w > sheet.getWidth() || y + h > sheet.getHeight()) return null;
        try {return sheet.getSubimage(x, y, w, h);}
        catch (Exception e) { return null; }
    }

    //Player tank sprite
    public static BufferedImage playerTank(int starLevel, int dirIdx) {
        int col = dirIdx * 2;
        int row = Math.min(starLevel, 3);
        return crop(col * S, row * S, S, S);
    }

    //Enemy tank sprite
    public static BufferedImage enemyTank(int typeIdx, int dirIdx) {
        switch (typeIdx) {
            case 0: // BASIC
                return crop((8 + dirIdx * 2) * S, S, S, S);
            case 1: // FAST
                return crop(dirIdx * 2 * S, 8 * S, S, S);
            case 2: // ARMOR
                return crop((8 + dirIdx * 2) * S, 8 * S, S, S);
            default:
                return null;
        }
    }

    
    // Terrain / wall tiles  (all 16×16 px; draw scaled to 32×32 in game)

    // Brick wall tile.
    public static BufferedImage brick() {return crop(256,  0, S/2, S/2);}

    // Steel wall tile.
    public static BufferedImage steel() {return crop(256,  16, S/2, S/2);}

    // Water tile.
    public static BufferedImage water() {return crop(256, 48, S/2, S/2);}

    // Bush tile.
    public static BufferedImage bush()  {return crop(272, 32, S/2, S/2);}

    // Eagle base (alive).
    public static BufferedImage eagleAlive() {return crop(304, 32, S, S);}

    // Eagle base (destroyed).
    public static BufferedImage eagleDestroyed() {return crop(320, 32, S, S);}

    // The full Miscellaneous sheet (contains title-screen art).
    public static BufferedImage miscSheet() {return miscSheet;}


    // Power-Up icons

    // Shield power up icon.
    public static BufferedImage shield() {return crop(256, 112, S, S);}
    // Clock power up icon.
    public static BufferedImage clock() {return crop(272, 112, S, S);}
    // Shovel power up icon.
    public static BufferedImage shovel() {return crop(288, 112, S, S);}
    // Star power up icon.
    public static BufferedImage star() {return crop(304,  112, S, S);}
    // Bomb power up icon.
    public static BufferedImage bomb() {return crop(320, 112, S, S);}
    // Tank power up icon.
    public static BufferedImage tank() {return crop(336, 112, S, S);}
}
