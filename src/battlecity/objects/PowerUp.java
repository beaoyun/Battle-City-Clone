package battlecity.objects;

import battlecity.GamePanel;
import java.awt.*;

public abstract class PowerUp extends GameObject {

    public static final int SIZE = 28;
    private boolean collected = false;

    protected PowerUp(int x, int y) { super(x, y, SIZE, SIZE); }

    @Override
    public void update(GamePanel panel) {
        PlayerTank p = panel.getPlayer();
        if (p != null && !p.isDead() && collidesWith(p)) {
            applyEffect(panel);
            collected = true;
        }
    }

    public boolean isCollected() { return collected; }

    public abstract void applyEffect(GamePanel panel);

    // Draw a styled power-up tile with a blinking border and icon text.
    protected void drawIconBox(Graphics g, Color bg, String icon) {
        boolean blink = (System.currentTimeMillis() / 350) % 2 == 0;

        // Outer border (blinking between white and the bg colour)
        g.setColor(blink ? Color.WHITE : bg.darker());
        g.fillRect(x, y, width, height);

        // Inner fill
        g.setColor(bg.darker().darker());
        g.fillRect(x + 2, y + 2, width - 4, height - 4);

        // Icon
        g.setColor(blink ? bg.brighter() : Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (width  - fm.stringWidth(icon)) / 2;
        int ty = y + (height + fm.getAscent() - fm.getDescent()) / 2 - 1;
        g.drawString(icon, tx, ty);
    }
}
