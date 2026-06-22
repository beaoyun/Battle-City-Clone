package battlecity.objects;

import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BrickWall extends Wall {

    public BrickWall(int x, int y) {super(x, y);}

    @Override 
    public boolean isPassable() {return false;}
    @Override 
    public boolean blocksBullets() {return true;}
    @Override 
    public void onHitByBullet(Bullet bullet) { kill(); }

    @Override
    public void draw(Graphics g) {
        BufferedImage sprite = Sprites.brick();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, x + width*2, y + height*2, 0, 0, 16, 16, null);
            return;
        }
        // Fallback:
        g.setColor(new Color(176, 80, 32));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(120, 56, 20));
        int half = height / 2;
        g.fillRect(x, y + half - 1, width, 2);
        g.fillRect(x + width / 2 - 1, y, 2, half);
        g.fillRect(x + width / 4 - 1, y + half, 2, half);
        g.fillRect(x + 3 * width / 4 - 1, y + half, 2, half);
    }
}
