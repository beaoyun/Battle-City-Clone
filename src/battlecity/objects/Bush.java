package battlecity.objects;

import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Bush extends Wall {

    public Bush(int x, int y) {super(x, y);}

    @Override 
    public boolean isPassable() {return true;}
    @Override 
    public boolean blocksBullets() {return false;}
    @Override 
    public void onHitByBullet(Bullet bullet) {}

    @Override
    public void draw(Graphics g) {
        BufferedImage sprite = Sprites.bush();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, x + width*2, y + height*2, 0, 0, 16, 16, null);
            return;
        }
        // Fallback:
        g.setColor(new Color(0, 100, 0));
        g.fillRect(x, y, width, height);
    }
}