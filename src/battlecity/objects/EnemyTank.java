package battlecity.objects;

import battlecity.Direction;
import battlecity.GamePanel;
import battlecity.Sprites;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class EnemyTank extends Tank {

    public enum Type {BASIC, FAST, ARMOR}

    private static final Random RNG = new Random();

    private final Type type;

    public EnemyTank(int x, int y, Type type, int level) {
        super(x, y);
        this.type = type;
        double mult = 1.0 + (level - 1) * 0.3;
        switch (type) {
            case BASIC: 
                speed = 2;
                hp = 1; 
                break;
            case FAST:  
                speed = (int) Math.max(3, Math.round(4 * mult)); 
                hp = 1; 
                break;
            case ARMOR: 
                speed = 1;
                hp = 3; 
                break;
        }
        this.direction = Direction.DOWN;
    }

    public void pickRandomDirection() {
        direction = Direction.values()[RNG.nextInt(4)];
    }

    @Override 
    public void update(GamePanel panel) {} // AI handled by EnemyAIThread

    @Override
    public void draw(Graphics g) {
        int typeIdx = (type == Type.BASIC) ? 0 : (type == Type.FAST) ? 1 : 2;
        int dirIdx  = directionIndex(direction);
        BufferedImage sprite = Sprites.enemyTank(typeIdx, dirIdx);

        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, width, height, null);
        } 
        else {
            // Fallback:
            switch (type) {
                case BASIC: 
                    g.setColor(new Color(180, 180, 180));
                    break;
                case FAST: 
                    g.setColor(new Color(100, 220, 100));
                    break;
                case ARMOR: 
                    g.setColor(new Color(200, 60,  60));
                    break;
            }
            g.fillRect(x, y, width, height);
            g.setColor(Color.DARK_GRAY);
            int bx = x + width / 2, by = y + height / 2;
            switch (direction) {
                case UP:
                    g.fillRect(bx - 2, y, 4, 8);
                    break;
                case DOWN:
                    g.fillRect(bx - 2, y + height - 8, 4, 8); 
                    break;
                case LEFT:
                    g.fillRect(x, by - 2, 8, 4);
                    break;
                case RIGHT:
                    g.fillRect(x + width - 8, by - 2, 8, 4);
                    break;
            }
        }
        // Show remaining HP for ARMOR tanks
        if (type == Type.ARMOR) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString("♥" + hp, x + 2, y - 1);
        }
    }

    public Type getType() {return type;}

    private static int directionIndex(Direction d) {
        switch (d) {
            case UP: return 0;
            case LEFT: return 1;
            case DOWN: return 2;
            default: return 3;
        }
    }
}
