package battlecity.objects;

import battlecity.Direction;
import battlecity.GamePanel;
import java.awt.Color;
import java.awt.Graphics;

public class Bullet extends GameObject {

    public static final int BULLET_SIZE = 6;
    private final Direction direction;
    private final Tank owner;
    private int speed = 6;

    public Bullet(int x, int y, Direction direction, Tank owner) {
        super(x, y, BULLET_SIZE, BULLET_SIZE);
        this.direction = direction;
        this.owner = owner;
    }

    @Override
    public void update(GamePanel panel) {
        x += direction.dx * speed;
        y += direction.dy * speed;

        if (x < 0 || y < 0 || x > GamePanel.PLAY_WIDTH || y > GamePanel.PLAY_HEIGHT) {
            kill();
            return;
        }

        for (Wall w : panel.getWalls()) {
            if (!w.isDead() && w.blocksBullets() && collidesWith(w)) {
                w.onHitByBullet(this);
                kill();
                return;
            }
        }

        Eagle eagle = panel.getEagle();
        if (eagle != null && !eagle.isDead() && collidesWith(eagle)) {
            eagle.kill();
            panel.onEagleDestroyed();
            kill();
            return;
        }

        if (owner instanceof PlayerTank) {
            for (EnemyTank e : panel.getEnemies()) {
                if (!e.isDead() && collidesWith(e)) {
                    e.takeDamage(1);
                    if (e.isDead()) panel.onEnemyDestroyed(e);
                    kill();
                    return;
                }
            }
        } else if (owner instanceof EnemyTank) {
            PlayerTank p = panel.getPlayer();
            if (p != null && !p.isDead() && !p.isShielded() && collidesWith(p)) {
                p.takeDamage(1);
                if (p.isDead()) panel.onPlayerHit();
                kill();
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(owner instanceof PlayerTank ? Color.YELLOW : Color.WHITE);
        g.fillRect(x, y, width, height);
    }

    public Tank getOwner() {return owner;}
}
