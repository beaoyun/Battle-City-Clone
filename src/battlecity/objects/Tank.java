package battlecity.objects;

import battlecity.Direction;
import battlecity.GamePanel;

public abstract class Tank extends GameObject {

    public static final int TANK_SIZE = 28;

    protected Direction direction = Direction.UP;
    protected int speed = 2;
    protected int hp = 1;
    protected long lastShotTime = 0;
    protected int shotCooldownMs = 500;

    protected Tank(int x, int y) {
        super(x, y, TANK_SIZE, TANK_SIZE);
    }

    public void tryMove(Direction dir, GamePanel panel) {
        this.direction = dir;
        int nx = x + dir.dx * speed;
        int ny = y + dir.dy * speed;
        if (panel.canTankMove(this, nx, ny)) {
            x = nx;
            y = ny;
        }
    }

    public void tryShoot(GamePanel panel) {
        if (this instanceof PlayerTank && !panel.canPlayerShoot()) return;

        long now = System.currentTimeMillis();
        if (now - lastShotTime < shotCooldownMs) return;
        lastShotTime = now;

        int bx = x + width / 2 - Bullet.BULLET_SIZE / 2;
        int by = y + height / 2 - Bullet.BULLET_SIZE / 2;
        panel.getBullets().add(new Bullet(bx, by, direction, this));
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) kill();
    }

    public Direction getDirection() { return direction; }
    public int getHp() { return hp; }
}
