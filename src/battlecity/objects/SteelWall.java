package battlecity.objects;

import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SteelWall extends Wall {

    public SteelWall(int x, int y) {super(x, y);}

    @Override 
    public boolean isPassable() {return false;}
    @Override 
    public boolean blocksBullets() {return true;}
    @Override
    public void onHitByBullet(Bullet bullet) {
        if (bullet.getOwner() instanceof PlayerTank) {
            PlayerTank p = (PlayerTank) bullet.getOwner();
            if (p.getStars() >= 3) {kill();}
        }
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage sprite = Sprites.steel();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, x + width*2, y + height*2, 0, 0, 16, 16, null);
            return;
        }
        // Fallback:
        g.setColor(new Color(140, 140, 140));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(100, 100, 100));
        int half = width / 2;
        g.fillRect(x + half - 1, y, 2, height);
        g.fillRect(x, y + half - 1, width, 2);
    }
}
