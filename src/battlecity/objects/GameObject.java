package battlecity.objects;

import battlecity.GamePanel;
import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class GameObject {

    protected int x, y;
    protected int width, height;
    protected boolean dead = false;

    protected GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(GamePanel panel);

    public abstract void draw(Graphics g);

    // Checks the bounds.
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    // Checks for collision/intersection of bounds.
    public boolean collidesWith(GameObject other){
        return getBounds().intersects(other.getBounds());
    }

    public boolean isDead() {return dead;}
    public void kill() {this.dead = true;}

    public int getX() {return x;}
    public int getY() {return y;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}

}
