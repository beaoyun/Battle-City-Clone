package battlecity;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;


// Displays GameStart.PNG scaled to fill the panel

public class SplashPanel extends JPanel {

    private BufferedImage image;
    private final Runnable onStart;

    public SplashPanel(Runnable onStart) {
        this.onStart = onStart;
        setBackground(Color.BLACK);
        setFocusable(true);

        // Load the opening image with multiple path fallbacks
        String[] paths = {
            "GameStart.PNG", "GameStart.png", "../GameStart.PNG",
            "../GameStart.png","BattleCity/GameStart.PNG"
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                try { image = ImageIO.read(f); break; }
                catch (IOException ex) {
                    System.err.println("Could not read splash image: " + p);
                }
            }
        }
        if (image == null) {
            System.err.println("GameStart.PNG not found — using text fallback.");
        }

        // Pressing SPACE starts the actual game
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    onStart.run();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Scale the image to fill the entire panel
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback:
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Monospaced", Font.BOLD, 48));
            FontMetrics fm = g.getFontMetrics();
            String title = "BATTLE CITY";
            g.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2 - 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 18));
            fm = g.getFontMetrics();
            String prompt = "Press Space to Start";
            g.drawString(prompt, (getWidth() - fm.stringWidth(prompt)) / 2, getHeight() / 2 + 40);
        }
    }
}
