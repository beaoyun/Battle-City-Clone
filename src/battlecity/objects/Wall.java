package battlecity.objects;

import battlecity.GamePanel;
import java.awt.Graphics;

public abstract class Wall extends GameObject {

    public static final int CELL = 16;

    protected Wall(int x, int y) {
        super(x, y, CELL, CELL);
    }

    public abstract boolean isPassable();
    public abstract boolean blocksBullets();

    // What happens when a bullet hits wall
    public abstract void onHitByBullet(Bullet bullet);

    @Override
    public void update(GamePanel panel) {} // walls are static

    @Override
    public abstract void draw(Graphics g);
}
