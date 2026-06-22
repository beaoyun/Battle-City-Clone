package battlecity.objects;

import battlecity.GamePanel;
import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ShovelPowerUp extends PowerUp {
    public ShovelPowerUp(int x, int y) { super(x, y); }

    @Override
    public void applyEffect(GamePanel panel) {panel.fortifyBase(10000);}

    @Override
    public void draw(Graphics g) {
        BufferedImage sprite = Sprites.shovel();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        // Fallback:
        drawIconBox(g, Color.LIGHT_GRAY, "S");
    }
}