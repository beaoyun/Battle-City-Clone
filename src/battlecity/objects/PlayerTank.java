package battlecity.objects;

import battlecity.Direction;
import battlecity.GamePanel;
import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerTank extends Tank {

    private int lives = 3;
    private int stars = 0;
    private long shieldUntil = 0;

    public PlayerTank(int x, int y) {
        super(x, y);
        this.speed = 2;
    }

    @Override 
    public void update(GamePanel panel) {}

    @Override
    public void draw(Graphics g) {
        int dirIdx = directionIndex(direction);
        BufferedImage sprite = Sprites.playerTank(Math.min(stars, 3), dirIdx);

        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
            if (isShielded()) {
                g2.setColor(new Color(100, 200, 255, 90));
                g2.fillRect(x, y, width, height);
            }
        } else {
            // Fallback:
            g.setColor(isShielded() ? new Color(100, 220, 255) : new Color(255, 220, 0));
            g.fillRect(x, y, width, height);
            g.setColor(Color.ORANGE);
            int bx = x + width / 2, by = y + height / 2;
            switch (direction) {
                case UP: 
                    g.fillRect(bx - 3, y, 6, 10); 
                    break;
                case DOWN: 
                    g.fillRect(bx - 3, y + height - 10, 6, 10); 
                    break;
                case LEFT: 
                    g.fillRect(x, by - 3, 10, 6);             
                    break;
                case RIGHT: 
                    g.fillRect(x + width - 10, by - 3, 10, 6); 
                    break;
            }
        }

        if (stars > 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString("★" + stars, x + 2, y - 2);
        }
    }

    public boolean isShielded() {return System.currentTimeMillis() < shieldUntil;}
    public void activateShield(int millis) {shieldUntil = System.currentTimeMillis() + millis;}
    public void addLife() {lives++;}

    public void addStar() {
        if (stars < 3) stars++;
        applyStarEffect();
    }

    public int getLives() {return lives;}
    public int getStars() {return stars;}
    public int getMaxBullets() {return (stars >= 2) ? 2 : 1;}
    public void setLives(int l) {this.lives = l;}

    public void respawn(int rx, int ry) {
        hp    = 1;
        dead  = false;
        x     = rx;
        y     = ry;
        direction = Direction.UP;
        activateShield(2000);
    }

    private void applyStarEffect() {
        if (stars >= 1) shotCooldownMs = 300;
        if (stars >= 2) shotCooldownMs = 200;
    }

    private static int directionIndex(Direction d) {
        switch (d) {
            case UP:    return 0;
            case LEFT:  return 1;
            case DOWN:  return 2;
            default:    return 3;
        }
    }
}
