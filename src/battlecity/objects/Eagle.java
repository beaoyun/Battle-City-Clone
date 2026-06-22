package battlecity.objects;

import battlecity.GamePanel;
import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Eagle extends GameObject {

    public Eagle(int x, int y) {super(x, y, 32, 32);}

    @Override 
    public void update(GamePanel panel) {}

    @Override
    public void draw(Graphics g) {
        if (isDead()) {drawDeadEagle(g);} 
        else {drawLiveEagle(g);}
    }

    private void drawLiveEagle(Graphics g) {
        BufferedImage sprite = Sprites.eagleAlive();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        // Fallback:
        g.setColor(new Color(100, 100, 100));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(250, 100, 100));
        g.fillRect(x + 6, y + 6, width - 6, height - 6);
        g.setColor(new Color(100, 100, 250));
        g.fillRect(x + 12, y + 12, width - 12, height - 12);
        
    }

    private void drawDeadEagle(Graphics g) {
        BufferedImage sprite = Sprites.eagleDestroyed();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        // Fallback:
        g.setColor(new Color(100, 100, 100));
        g.fillRect(x, y, width, height);
    }
}
