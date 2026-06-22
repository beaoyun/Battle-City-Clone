package battlecity.editor;

import battlecity.level.LevelLoader;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Right-side legend panel for the map editor
public class MapEditorLegendPanel extends JPanel {

    public static final int WIDTH = 148;

    private static final char[] TILES = {'.', 'B', 'S', 'G', 'W', 'E', 'P', 'X'};
    private static final String[] LABELS = {
            "Empty", "Brick", "Steel", "Bush", "Water", "Eagle", "Player spawn", "Enemy spawn"
    };

    public MapEditorLegendPanel() {
        setPreferredSize(new Dimension(WIDTH, LevelLoader.ROWS * LevelLoader.CELL));
        setBackground(new Color(60, 60, 60));
        setBorder(new EmptyBorder(12, 10, 12, 10));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = 16;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("TILES", 8, y);
        y += 22;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        int sc = LevelLoader.CELL;
        for (int i = 0; i < TILES.length; i++) {
            MapEditorPanel.drawCell(g2, 4, y, TILES[i]);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(LABELS[i], 4 + sc + 8, y + sc / 2 + 4);
            y += sc + 6;
        }

        y += 8;
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String[] hints = {
                "Click a cell to", "cycle tile type.",
                "",
                "Order: Empty →", "Brick → Steel →", "Bush → Water →",
                "Eagle → Player →", "Enemy spawn",
                "",
                "Do NOT place the", "player or enemy spawn", 
                "close to other", "objects or map edges",
                "or else they", "might get stuck."
        };
        for (String line : hints) {
            g2.drawString(line, 4, y);
            y += 14;
        }
    }
}
