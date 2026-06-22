package battlecity.editor;

import battlecity.level.LevelLoader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class MapEditorPanel extends JPanel {

    private static final char[] TILE_CYCLE = {'.', 'B', 'S', 'G', 'W', 'E', 'P', 'X'};

    private final char[][] grid = new char[LevelLoader.ROWS][LevelLoader.COLS];

    public MapEditorPanel() {
        setPreferredSize(new Dimension(LevelLoader.COLS * LevelLoader.CELL, LevelLoader.ROWS * LevelLoader.CELL));
        clearGrid();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / LevelLoader.CELL;
                int row = e.getY() / LevelLoader.CELL;
                if (row >= 0 && row < LevelLoader.ROWS && col >= 0 && col < LevelLoader.COLS) {
                    cycleTile(row, col);
                    repaint();
                }
            }
        });
    }

    public void clearGrid() {
        for (int r = 0; r < LevelLoader.ROWS; r++) {
            for (int c = 0; c < LevelLoader.COLS; c++) {
                grid[r][c] = '.';
            }
        }
        repaint();
    }

    private void cycleTile(int row, int col) {
        char current = grid[row][col];
        int idx = 0;
        for (int i = 0; i < TILE_CYCLE.length; i++) {
            if (TILE_CYCLE[i] == current) {
                idx = (i + 1) % TILE_CYCLE.length;
                break;
            }
        }
        grid[row][col] = TILE_CYCLE[idx];
    }

    public void saveToFile(File file) throws IOException {
        new File(file.getParent()).mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (int r = 0; r < LevelLoader.ROWS; r++) {
                pw.println(new String(grid[r]));
            }
        }
    }

    // Converts the current grid to a LevelData object so the game can play it directly.
    public battlecity.level.LevelLoader.LevelData toLevel() {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (char[] row : grid) lines.add(new String(row));
        return battlecity.level.LevelLoader.parseLines(lines);
    }

    public void loadFromFile(File file) throws IOException {
        ArrayList<String> lines = LevelLoader.readMapLines(file);
        if (LevelLoader.isLegacyMap(lines)) {
            lines = LevelLoader.upscaleLegacyMap(lines);
        }
        clearGrid();
        for (int r = 0; r < Math.min(ROWS(), lines.size()); r++) {
            String line = lines.get(r);
            for (int c = 0; c < Math.min(LevelLoader.COLS, line.length()); c++) {
                grid[r][c] = line.charAt(c);
            }
        }
        repaint();
    }

    private int ROWS() {return LevelLoader.ROWS;}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int r = 0; r < LevelLoader.ROWS; r++) {
            for (int c = 0; c < LevelLoader.COLS; c++) {
                int x = c * LevelLoader.CELL;
                int y = r * LevelLoader.CELL;
                drawCell(g, x, y, grid[r][c]);
            }
        }
    }

    static void drawCell(Graphics g, int x, int y, char tile) {
        g.setColor(colorFor(tile));
        g.fillRect(x, y, LevelLoader.CELL, LevelLoader.CELL);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, LevelLoader.CELL, LevelLoader.CELL);
    }

    static Color colorFor(char tile) {
        switch (tile) {
            case 'B': return new Color(160, 80, 40);
            case 'S': return Color.GRAY;
            case 'G': return new Color(20, 130, 40);
            case 'W': return new Color(0, 0, 200);
            case 'E': return new Color(200, 200, 200);
            case 'P': return Color.YELLOW;
            case 'X': return Color.RED;
            default:  return Color.BLACK;
        }
    }
}
