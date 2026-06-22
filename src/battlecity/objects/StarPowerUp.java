package battlecity.objects;

import battlecity.GamePanel;
import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StarPowerUp extends PowerUp {
    public StarPowerUp(int x, int y) {super(x, y);}

    @Override 
    public void applyEffect(GamePanel panel) {panel.getPlayer().addStar();}

    @Override 
    public void draw(Graphics g) {
        BufferedImage sprite = Sprites.star();
        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
            return;
        }
        // Fallback:
        drawIconBox(g, Color.YELLOW, "*");
    }
}