package battlecity.ai;

import battlecity.GamePanel;
import battlecity.level.GameState;
import battlecity.objects.EnemyTank;
import java.util.Random;

public class EnemyAIThread extends Thread {

    private static final Random RNG = new Random();

    private final GamePanel panel;
    private final EnemyTank enemy;
    private volatile boolean running = true;
    private long lastDirChange = 0;

    private final long directionChangeMs;
    private final int shootChance;

    public EnemyAIThread(GamePanel panel, EnemyTank enemy, int level) {
        super("EnemyAI-" + enemy.getType());
        this.panel = panel;
        this.enemy = enemy;
        setDaemon(true);
        double mult = 1.0 + level * 0.5;
        directionChangeMs = (long) (2000 / mult);
        shootChance = Math.max(50, (int) (200 / mult));
        lastDirChange = System.currentTimeMillis();
    }

    public EnemyTank getEnemy() { return enemy; }

    public void stopAI() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running && !enemy.isDead() && panel.getState() != GameState.GAME_OVER) {
            if (panel.isPaused() || panel.isEnemiesFrozen()) {
                sleepQuiet(100);
                continue;
            }

            long now = System.currentTimeMillis();
            final boolean[] changeDir = {now - lastDirChange > directionChangeMs};
            if (changeDir[0]) lastDirChange = now;

            panel.runWorldSafe(() -> {
                if (enemy.isDead()) return;
                if (changeDir[0]) enemy.pickRandomDirection();
                enemy.tryMove(enemy.getDirection(), panel);
                if (RNG.nextInt(shootChance) == 0) enemy.tryShoot(panel);
            });

            sleepQuiet(80);
        }
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
