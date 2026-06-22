package battlecity;

import battlecity.level.GameState;

public class GameLoop extends Thread {

    private static final int TARGET_FPS = 60;
    private static final int FRAME_TIME = 1000 / TARGET_FPS;

    private final GamePanel panel;
    private volatile boolean running = true;

    public GameLoop(GamePanel panel) {
        super("BattleCity-GameLoop");
        this.panel = panel;
        setDaemon(true);
    }

    public void stopLoop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            long frameStart = System.currentTimeMillis();

            if (!panel.isPaused() && panel.getState() != GameState.GAME_OVER) {
                panel.updateGame();
            }
            panel.repaint();

            long elapsed = System.currentTimeMillis() - frameStart;
            long sleep = FRAME_TIME - elapsed;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
