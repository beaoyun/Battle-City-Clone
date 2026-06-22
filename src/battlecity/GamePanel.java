package battlecity;

import battlecity.ai.EnemyAIThread;
import battlecity.level.GameState;
import battlecity.level.LevelLoader;
import battlecity.objects.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import javax.swing.*;

public class GamePanel extends JPanel {

    public static final int PLAY_WIDTH  = LevelLoader.COLS * LevelLoader.CELL; // 800
    public static final int PLAY_HEIGHT = LevelLoader.ROWS * LevelLoader.CELL; // 576
    public static final int SIDE_WIDTH  = 128;
    public static final int PANEL_WIDTH = PLAY_WIDTH + SIDE_WIDTH; // 928
    public static final int PANEL_HEIGHT = PLAY_HEIGHT; // 576

    public static final int ENEMIES_PER_LEVEL = 20;
    public static final int MAX_ON_SCREEN = 4;

    private final ArrayList<EnemyTank> enemies = new ArrayList<>();
    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<Wall> walls  = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();
    private final Queue<EnemyTank.Type> spawnQueue = new LinkedList<>();
    private final HashMap<Integer,Boolean> keysHeld = new HashMap<>();
    private final ArrayList<EnemyAIThread> enemyThreads = new ArrayList<>();
    private final ArrayList<Point> enemySpawnPoints = new ArrayList<>();
    private final ArrayList<Wall> fortifiedBackup  = new ArrayList<>();
    private final Random rng = new Random();
    private final Object worldLock = new Object();

    private PlayerTank player;
    private Eagle eagle;
    private Point playerSpawn = new Point(4 * LevelLoader.CELL, 16 * LevelLoader.CELL);
    private int level = 1;
    private int enemiesKilled = 0;
    private int spawnIndex = 0;
    private int score = 0;
    private long gameStartMs;
    private long levelClearAt = 0;
    private volatile boolean paused = false;
    private GameState state = GameState.PLAYING;
    private long frozenUntil = 0;
    private long fortifiedUntil = 0;

    private GameLoop gameLoop;
    private Runnable onGameOver;
    private boolean  gameOverNotified = false;
    private boolean  victory = false;

    public boolean isVictory() {return victory;}

    // For sidebar animation (blinking icons)
    private long lastBlink = 0;
    private boolean blinkState = false;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                keysHeld.put(e.getKeyCode(), true);
                if (e.getKeyCode() == KeyEvent.VK_P) togglePause();
            }
            @Override public void keyReleased(KeyEvent e) {
                keysHeld.put(e.getKeyCode(), false);
            }
        });
    }

    public void setOnGameOver(Runnable callback) {this.onGameOver = callback;}


    // Game lifecycle

    // Stop the running game loop and all AI threads (no new game started).
    public void stopGame() {
        stopAllEnemyThreads();
        if (gameLoop != null) { gameLoop.stopLoop(); gameLoop = null; }
        synchronized (worldLock) {
            enemies.clear(); 
            bullets.clear(); 
            powerUps.clear();
            state = GameState.PLAYING;
        }
    }

    public void startNewGame() {
        stopAllEnemyThreads();
        if (gameLoop != null) gameLoop.stopLoop();

        synchronized (worldLock) {
            enemies.clear();
            bullets.clear();
            walls.clear();
            powerUps.clear();
            spawnQueue.clear();
            fortifiedBackup.clear();
            enemiesKilled = 0;
            spawnIndex = 0;
            score = 0;
            level = 1;
            paused = false;
            state = GameState.PLAYING;
            frozenUntil = 0;
            fortifiedUntil = 0;
            gameStartMs = System.currentTimeMillis();
            gameOverNotified = false;
            victory = false;
            loadLevel(level);
        }

        gameLoop = new GameLoop(this);
        gameLoop.start();
    }

    // Start a game using a custom level from the Map Editor.
    public void startCustomGame(LevelLoader.LevelData data) {
        stopAllEnemyThreads();
        if (gameLoop != null) gameLoop.stopLoop();

        synchronized (worldLock) {
            enemies.clear();
            bullets.clear();
            walls.clear();
            powerUps.clear();
            spawnQueue.clear();
            fortifiedBackup.clear();
            enemiesKilled = 0;
            spawnIndex = 0;
            score = 0;
            level = 1;
            paused = false;
            state = GameState.PLAYING;
            frozenUntil = 0;
            fortifiedUntil = 0;
            gameStartMs = System.currentTimeMillis();
            gameOverNotified = false;
            victory = false;

            walls.addAll(data.walls);
            eagle = data.eagle;
            playerSpawn = data.playerSpawn;
            enemySpawnPoints.clear();
            enemySpawnPoints.addAll(data.enemySpawns);
            if (enemySpawnPoints.isEmpty()) {
                enemySpawnPoints.add(new Point(0, 0));
                enemySpawnPoints.add(new Point(12 * LevelLoader.CELL, 0));
                enemySpawnPoints.add(new Point(24 * LevelLoader.CELL, 0));
            }

            buildBaseBricks();
            spawnPlayer();
            fillSpawnQueue();
            trySpawnEnemy();
        }

        gameLoop = new GameLoop(this);
        gameLoop.start();
    }

    public void loadLevel(int lvl) {
        try {
            LevelLoader.LevelData data = LevelLoader.loadBuiltIn(lvl);
            walls.clear();
            walls.addAll(data.walls);
            eagle = data.eagle;
            playerSpawn = data.playerSpawn;
            enemySpawnPoints.clear();
            enemySpawnPoints.addAll(data.enemySpawns);
        } catch (Exception ex) {
            System.err.println("Level load failed: " + ex.getMessage());
            eagle = new Eagle(12 * LevelLoader.CELL, 16 * LevelLoader.CELL);
            playerSpawn = new Point(4  * LevelLoader.CELL, 16 * LevelLoader.CELL);
        }
        buildBaseBricks();
        spawnPlayer();
        fillSpawnQueue();
        trySpawnEnemy();
    }

    // The 12 cell offsets (relative to eagle top-left) that form the protective ring around the base.
    // All three base-management methods share this shape.
    private static int[][] baseOffsets(int c) {
        return new int[][] {
            {-c, 0}, {-c, c}, {-c, 2*c}, { 0, 2*c}, { c, 2*c}, {2*c, 2*c},
            {2*c, c}, {2*c, 0}, {2*c,-c}, { c,-c}, { 0,-c}, {-c,-c}
        };
    }

    private void buildBaseBricks() {
        if (eagle == null) return;
        int ex = eagle.getX(), ey = eagle.getY();
        int c = LevelLoader.CELL;
        for (int[] o : baseOffsets(c)) {
            int wx = ex + o[0], wy = ey + o[1];
            if (wx < 0 || wy < 0 || wx + c > PLAY_WIDTH || wy + c > PLAY_HEIGHT) {continue;}
            boolean exists = false;
            for (Wall w : walls) {
                if (w.getX() == wx && w.getY() == wy && !(w instanceof Bush)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {walls.add(new BrickWall(wx, wy));}
        }
    }

    private void fillSpawnQueue() {
        spawnQueue.clear();
        for (int i = 0; i < ENEMIES_PER_LEVEL; i++) {
            if (level == 1) {
                spawnQueue.add(rng.nextInt(5) == 0 ? EnemyTank.Type.FAST : EnemyTank.Type.BASIC);
            } else if (level == 2) {
                int r = rng.nextInt(10);
                spawnQueue.add(r < 4 ? EnemyTank.Type.FAST : r < 6 ? EnemyTank.Type.ARMOR : EnemyTank.Type.BASIC);
            } else {
                spawnQueue.add(rng.nextInt(10) < 5 ? EnemyTank.Type.ARMOR : EnemyTank.Type.FAST);
            }
        }
    }

    public void spawnPlayer() {
        player = new PlayerTank(playerSpawn.x, playerSpawn.y);
    }

    // Pause state
    public boolean togglePause() {
        if (state == GameState.GAME_OVER || state == GameState.LEVEL_CLEAR) {
            return paused;
        }
        paused = !paused;
        state  = paused ? GameState.PAUSED : GameState.PLAYING;
        return paused;
    }

    public boolean isPaused() {return paused;}
    public boolean isEnemiesFrozen() {return System.currentTimeMillis() < frozenUntil;}
    public GameState getState() {return state;}

    public void runWorldSafe(Runnable r) {
        synchronized (worldLock) {r.run();}
    }

    // Update loop
    public void updateGame() {
        if (state == GameState.GAME_OVER) return;

        synchronized (worldLock) {
            if (state == GameState.LEVEL_CLEAR) {
                if (System.currentTimeMillis() - levelClearAt > 2500) {
                    if (level >= 3) {
                        triggerGameOver(true);
                    } else {
                        level++;
                        state = GameState.PLAYING;
                        enemiesKilled = 0;
                        loadLevel(level);
                    }
                }
                return;
            }

            if (!paused && state == GameState.PLAYING) {
                handleInput();
                if (player != null && !player.isDead()) player.update(this);

                Iterator<Bullet> bit = bullets.iterator();
                while (bit.hasNext()) { Bullet b = bit.next(); b.update(this); if (b.isDead()) bit.remove(); }

                Iterator<PowerUp> pit = powerUps.iterator();
                while (pit.hasNext()) { PowerUp p = pit.next(); p.update(this); if (p.isCollected()) pit.remove(); }

                trySpawnEnemy();
                checkWinLose();
                updateFortifyTimer();
            }
        }

        // Sidebar blink tick
        long now = System.currentTimeMillis();
        if (now - lastBlink > 400) {blinkState = !blinkState; lastBlink = now;}
    }

    private void handleInput() {
        if (player == null || player.isDead()) return;
        if (keyDown(KeyEvent.VK_W)) player.tryMove(Direction.UP, this);
        if (keyDown(KeyEvent.VK_S)) player.tryMove(Direction.DOWN, this);
        if (keyDown(KeyEvent.VK_A)) player.tryMove(Direction.LEFT, this);
        if (keyDown(KeyEvent.VK_D)) player.tryMove(Direction.RIGHT, this);
        if (keyDown(KeyEvent.VK_SPACE)) player.tryShoot(this);
    }

    private boolean keyDown(int code) {
        Boolean v = keysHeld.get(code);
        return v != null && v;
    }

    // Collision / movement
    public boolean canTankMove(Tank tank, int nx, int ny) {
        if (nx < 0 || ny < 0 || nx + Tank.TANK_SIZE > PLAY_WIDTH || ny + Tank.TANK_SIZE > PLAY_HEIGHT) {return false;}
        Rectangle next = new Rectangle(nx, ny, Tank.TANK_SIZE, Tank.TANK_SIZE);
        for (Wall w : walls)
            if (!w.isDead() && !w.isPassable() && next.intersects(w.getBounds())) {return false;}
        if (player != null && !player.isDead() && player != tank && next.intersects(player.getBounds()))
            return false;
        for (EnemyTank e : enemies)
            if (!e.isDead() && e != tank && next.intersects(e.getBounds())) {return false;}
        if (eagle != null && !eagle.isDead() && next.intersects(eagle.getBounds())) {return false;}
        return true;
    }

    public boolean canPlayerShoot() {
        if (player == null) return false;
        int count = 0;
        for (Bullet b : bullets) if (!b.isDead() && b.getOwner() == player) {count++;}
        return count < player.getMaxBullets();
    }

    // Spawn / destroy
    private void trySpawnEnemy() {
        if (spawnQueue.isEmpty() || enemies.size() >= MAX_ON_SCREEN || enemySpawnPoints.isEmpty()) {return;}
        Point sp = enemySpawnPoints.get(spawnIndex % enemySpawnPoints.size());
        spawnIndex++;
        EnemyTank enemy = new EnemyTank(sp.x, sp.y, spawnQueue.poll(), level);
        enemies.add(enemy);
        EnemyAIThread ai = new EnemyAIThread(this, enemy, level);
        enemyThreads.add(ai);
        ai.start();
    }

    public void onEnemyDestroyed(EnemyTank e) {
        onEnemyDestroyed(e, true);
    }

    private void onEnemyDestroyed(EnemyTank e, boolean dropPowerUp) {
        if (!enemies.remove(e)) {return;} // already removed
        e.kill();
        stopThreadForEnemy(e);
        enemiesKilled++;
        score += 100;
        if (dropPowerUp) maybeDropPowerUp(e.getX(), e.getY());
        trySpawnEnemy();
    }

    public void onEagleDestroyed() {
        if (eagle != null && !eagle.isDead()) { 
            eagle.kill(); 
            triggerGameOver(false); 
        }
    }

    public void onPlayerHit() {
        if (player == null) {return;}
        int remaining = player.getLives() - 1;
        player.setLives(remaining);
        if (remaining > 0) player.respawn(playerSpawn.x, playerSpawn.y);
        else { 
            player.kill(); 
            triggerGameOver(false);
        }
    }

    private void checkWinLose() {
        if (eagle != null && eagle.isDead()) { 
            triggerGameOver(false); 
            return; 
        }
        if (enemiesKilled >= ENEMIES_PER_LEVEL && enemies.isEmpty() && spawnQueue.isEmpty()) {
            state = GameState.LEVEL_CLEAR;
            levelClearAt = System.currentTimeMillis();
            score += 500 * level;
        }
    }

    private void triggerGameOver(boolean win) {
        if (state == GameState.GAME_OVER) {return;}
        victory = win;
        state = GameState.GAME_OVER;
        stopAllEnemyThreads();
        if (!gameOverNotified && onGameOver != null) {
            gameOverNotified = true;
            SwingUtilities.invokeLater(onGameOver);
        }
    }

    public void stopAllEnemyThreads() {
        for (EnemyAIThread t : enemyThreads) t.stopAI();
        enemyThreads.clear();
    }

    private void stopThreadForEnemy(EnemyTank e) {
        Iterator<EnemyAIThread> it = enemyThreads.iterator();
        while (it.hasNext()) {
            EnemyAIThread t = it.next();
            if (t.getEnemy() == e) { t.stopAI(); it.remove(); break; }
        }
    }

    // Power-up effects
    public void freezeEnemies(long millis) { frozenUntil = System.currentTimeMillis() + millis; }

    public void fortifyBase(long millis) {
        if (eagle == null) return;
        fortifiedBackup.clear();
        int ex = eagle.getX(), ey = eagle.getY(), c = LevelLoader.CELL;

        for (int[] o : baseOffsets(c)) {
            int wx = ex + o[0], wy = ey + o[1];
            if (wx < 0 || wy < 0 || wx + c > PLAY_WIDTH || wy + c > PLAY_HEIGHT) continue;

            // Swap out any BrickWall at this position and remember it for restoration
            Iterator<Wall> it = walls.iterator();
            while (it.hasNext()) {
                Wall w = it.next();
                if (w.getX() == wx && w.getY() == wy && w instanceof BrickWall) {
                    fortifiedBackup.add(w);   // keep original for restore
                    it.remove();
                    break;
                }
            }
            boolean hasSteelAlready = false;
            for (Wall w : walls) {
                if (w.getX() == wx && w.getY() == wy && w instanceof SteelWall) {
                    hasSteelAlready = true; 
                    break;
                }
            }
            if (!hasSteelAlready) walls.add(new SteelWall(wx, wy));
        }
        fortifiedUntil = System.currentTimeMillis() + millis;
    }

    private void updateFortifyTimer() {
        if (fortifiedUntil > 0 && System.currentTimeMillis() >= fortifiedUntil) {
            fortifiedUntil = 0;
            restoreBaseBricks();
        }
    }

    private void restoreBaseBricks() {
        if (eagle == null) return;
        int ex = eagle.getX(), ey = eagle.getY(), c = LevelLoader.CELL;

        // Remove the steel walls that fortifyBase() placed at every perimeter position
        for (int[] o : baseOffsets(c)) {
            final int wx = ex + o[0], wy = ey + o[1];
            walls.removeIf(w -> w.getX() == wx && w.getY() == wy && w instanceof SteelWall);
        }

        // Put the original brick walls back
        for (Wall w : fortifiedBackup) walls.add(new BrickWall(w.getX(), w.getY()));
        fortifiedBackup.clear();
    }

    private void maybeDropPowerUp(int x, int y) {
        if (rng.nextInt(5) != 0) return;
        PowerUp p;
        switch (rng.nextInt(6)) {
            case 0: p = new TankPowerUp(x, y);   break;
            case 1: p = new StarPowerUp(x, y);   break;
            case 2: p = new BombPowerUp(x, y);   break;
            case 3: p = new ClockPowerUp(x, y);  break;
            case 4: p = new ShovelPowerUp(x, y); break;
            default: p = new ShieldPowerUp(x, y); break;
        }
        powerUps.add(p);
    }

    public void destroyAllEnemies() {
        new ArrayList<>(enemies).forEach(e -> onEnemyDestroyed(e, false));
    }

    // Rendering
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Play field background
        g.setColor(new Color(10, 10, 10));
        g.fillRect(0, 0, PLAY_WIDTH, PLAY_HEIGHT);

        // World objects (bushes are drawn last so tanks hide beneath them)
        synchronized (worldLock) {
            for (Wall w : walls) if (!w.isDead() && !(w instanceof Bush)) w.draw(g);
            if (eagle  != null) eagle.draw(g);
            if (player != null && !player.isDead()) player.draw(g);
            for (EnemyTank e : enemies) if (!e.isDead()) e.draw(g);
            for (Bullet   b : bullets) if (!b.isDead()) b.draw(g);
            for (PowerUp  p : powerUps) p.draw(g);
            for (Wall w : walls) if (!w.isDead() && w instanceof Bush) w.draw(g);
        }

        // --- Overlay messages ---
        if (paused) {
            drawCenteredMessage(g, "PAUSED", new Color(50, 50, 250), 40f);
        }
        if (state == GameState.LEVEL_CLEAR) {
            drawCenteredMessage(g, "STAGE CLEAR!", new Color(80, 250, 80), 40f);
        }
        if (state == GameState.GAME_OVER) {
            if (victory) {
                drawCenteredMessage(g, "YOU WIN!", new Color(255, 230, 0), 40f);
            } else {
                drawCenteredMessage(g, "GAME OVER", new Color(250, 30, 30), 40f);
            }
        }

        // --- Right-side panel ---
        drawSidePanel(g);
    }

    private void drawCenteredMessage(Graphics g, String text, Color color, float size) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(new Font("Monospaced", Font.BOLD, (int) size));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (PLAY_WIDTH - fm.stringWidth(text)) / 2;
        int ty = PLAY_HEIGHT / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx + 2, ty + 2);
        g2.setColor(color);
        g2.drawString(text, tx, ty);
    }

    private void drawSidePanel(Graphics g) {
        int px = PLAY_WIDTH;

        // Background
        g.setColor(new Color(80, 80, 80));
        g.fillRect(px, 0, SIDE_WIDTH, PANEL_HEIGHT);

        // Left border line
        g.setColor(Color.BLACK);
        g.drawLine(px, 0, px, PANEL_HEIGHT);
        g.drawLine(px + 1, 0, px + 1, PANEL_HEIGHT);

        Font labelFont  = new Font("Monospaced", Font.BOLD, 12);
        Font levelFont  = new Font("Monospaced", Font.BOLD, 20);
        Color orange    = new Color(255, 140, 0);
        Color iconGray  = new Color(190, 190, 190);
        Color iconGold  = new Color(255, 200, 0);

        int y = 10;

        // ENEMY section
        g.setFont(labelFont);
        g.setColor(orange);
        drawCenteredInPanel(g, "ENEMY", px, y + 11);
        y += 18;

        // Tank icons for remaining enemies
        int queued = spawnQueue.size();
        int onScreen = enemies.size();
        int totalRemaining = queued + onScreen; // icons still to account for
        for (int i = 0; i < 20; i++) {
            int col = i % 2;
            int row = i / 2;
            int ix  = px + 18 + col * 22;
            int iy  = y  + row * 20;
            if (i < totalRemaining) {
                drawSmallTankIcon(g, ix, iy, iconGray, Direction.DOWN);
            }
        }
        y += 10 * 20 + 10;

        // Divider
        g.setColor(new Color(50, 50, 50));
        g.fillRect(px + 8, y, SIDE_WIDTH - 16, 2);
        y += 10;

        // PLAYER section
        g.setColor(orange);
        g.setFont(labelFont);
        drawCenteredInPanel(g, "PLAYER", px, y + 11);
        y += 18;
        // Tank icons for remaining player lives
        int lives = (player != null) ? player.getLives() : 0;
        for (int i = 0; i < Math.min(lives, 6); i++) {
            int col = i % 3;
            int row = i / 3;
            int ix  = px + 10 + col * 22;
            int iy  = y  + row * 20;
            drawSmallTankIcon(g, ix, iy, iconGold, Direction.UP);
        }
        y += ((Math.min(lives, 6) + 2) / 3) * 20 + 10;

        // Divider
        g.setColor(new Color(50, 50, 50));
        g.fillRect(px + 8, y, SIDE_WIDTH - 16, 2);
        y += 10;

        // Small flag for level
        g.setColor(orange);
        int[] fx = { px + 18, px + 18, px + 34 };
        int[] fy = { y, y + 14, y + 7 };
        g.fillPolygon(fx, fy, 3);
        g.setColor(new Color(180, 100, 0));
        g.fillRect(px + 16, y, 2, 18); // pole
        g.setColor(Color.WHITE);
        g.setFont(levelFont);
        g.drawString(String.valueOf(level), px + 38, y + 16);

        y += 30;

        // SCORE
        g.setFont(labelFont);
        g.setColor(orange);
        drawCenteredInPanel(g, "SCORE", px, y + 11);
        y += 14;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        String scoreStr = String.valueOf(score);
        drawCenteredInPanel(g, scoreStr, px, y + 13);
    }

    private void drawCenteredInPanel(Graphics g, String text, int panelX, int textY) {
        FontMetrics fm = g.getFontMetrics();
        int tx = panelX + (SIDE_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, tx, textY);
    }

    // Draws a 14×14 tank icon pointing in the given direction.
    private void drawSmallTankIcon(Graphics g, int x, int y, Color color, Direction dir) {
        g.setColor(color);
        g.fillRect(x + 2, y + 3, 10, 9);
        g.fillRect(x,     y + 2, 3, 11);
        g.fillRect(x + 11, y + 2, 3, 11);
        // Barrel (direction-dependent)
        g.setColor(color.darker());
        switch (dir) {
            case UP:
                g.fillRect(x + 5, y + 2, 4, 4);
                g.fillRect(x + 6, y - 1, 2, 4);
                break;
            case DOWN:
                g.fillRect(x + 5, y + 9, 4, 4);
                g.fillRect(x + 6, y + 12, 2, 4);
                break;
            case LEFT:
                g.fillRect(x + 2, y + 5, 4, 4);
                g.fillRect(x - 1, y + 6, 4, 2);
                break;
            default:
                g.fillRect(x + 8, y + 5, 4, 4);
                g.fillRect(x + 11, y + 6, 4, 2);
                break;
        }
    }

    public long getElapsedSeconds() {return (System.currentTimeMillis() - gameStartMs) / 1000;}
    public int  getScore() {return score;}
    public int  getLevel() {return level;}
    public int  getSpawnQueueSize() {return spawnQueue.size();}

    public ArrayList<EnemyTank> getEnemies() {return enemies;}
    public ArrayList<Bullet>    getBullets() {return bullets;}
    public ArrayList<Wall>      getWalls() {return walls;}
    public ArrayList<PowerUp>   getPowerUps() {return powerUps;}
    public PlayerTank           getPlayer() {return player;}
    public Eagle                getEagle() {return eagle;}

    public void setEagle(Eagle e) {this.eagle = e;}
    public void setPlayerSpawn(Point p) {this.playerSpawn = p;}
    public void setEnemySpawnPoints(List<Point> pts) {
        enemySpawnPoints.clear();
        enemySpawnPoints.addAll(pts);
    }
}
