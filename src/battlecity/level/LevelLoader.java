package battlecity.level;

import battlecity.GamePanel;
import battlecity.objects.*;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


//Loads a text map grid into GamePanel collections.

public class LevelLoader {

    public static final int COLS = 50;
    public static final int ROWS = 36;
    public static final int CELL = Wall.CELL;

    public static class LevelData {
        public final ArrayList<Wall> walls = new ArrayList<>();
        public final ArrayList<Point> enemySpawns = new ArrayList<>();
        public Point playerSpawn;
        public Eagle eagle;
    }

    public static LevelData loadFromFile(File file) throws IOException {
        ArrayList<String> lines = readMapLines(file);
        if (isLegacyMap(lines)) {
            lines = upscaleLegacyMap(lines);
        }
        return parseLines(lines);
    }

    public static ArrayList<String> readMapLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        }
        return lines;
    }

    // Legacy maps are 25×18 cells at 32px, normal format is 50×36 at 16px (same pixel size).
    public static boolean isLegacyMap(ArrayList<String> lines) {
        if (lines.isEmpty()) return false;
        int maxLen = 0;
        for (String line : lines) maxLen = Math.max(maxLen, line.length());
        return lines.size() <= 18 && maxLen <= 25;
    }

    // Turns 25×18 maps into 50×36.
    public static ArrayList<String> upscaleLegacyMap(ArrayList<String> lines) {
        ArrayList<String> out = new ArrayList<>();
        for (String line : lines) {
            String row = expandMapLine(line);
            out.add(row);
            out.add(row);
        }
        return out;
    }

    static String expandMapLine(String line) {
        StringBuilder sb = new StringBuilder(line.length() * 2);
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            sb.append(c).append(c);
        }
        return sb.toString();
    }

    public static LevelData loadBuiltIn(int level) throws IOException {
        String filename = "level" + level + ".map";
        File[] candidates = {
                new File("maps", filename),
                new File(System.getProperty("user.dir"), "maps/" + filename),
                new File("Battle City/maps", filename)
        };
        for (File f : candidates) {
            if (f.exists()) return loadFromFile(f);
        }
        return buildDefaultLevel(level);
    }

    public static LevelData parseLines(ArrayList<String> lines) {
        LevelData data = new LevelData();
        int defaultPlayerX = 4 * CELL;
        int defaultPlayerY = 16 * CELL;
        int defaultEagleX = 12 * CELL;
        int defaultEagleY = 17 * CELL;

        data.playerSpawn = new Point(defaultPlayerX, defaultPlayerY);
        data.eagle = new Eagle(defaultEagleX, defaultEagleY);

        for (int row = 0; row < Math.min(ROWS, lines.size()); row++) {
            String line = lines.get(row);
            for (int col = 0; col < Math.min(COLS, line.length()); col++) {
                int x = col * CELL;
                int y = row * CELL;
                char c = line.charAt(col);
                switch (c) {
                    case 'B':
                        data.walls.add(new BrickWall(x, y));
                        break;
                    case 'S':
                        data.walls.add(new SteelWall(x, y));
                        break;
                    case 'G':
                        data.walls.add(new Bush(x, y));
                        break;
                    case 'W':
                        data.walls.add(new Water(x, y));
                        break;
                    case 'E':
                        data.eagle = new Eagle(x, y);
                        break;
                    case 'P':
                        data.playerSpawn = new Point(x, y);
                        break;
                    case 'X':
                        data.enemySpawns.add(new Point(x, y));
                        break;
                    default:
                        break;
                }
            }
        }

        if (data.enemySpawns.isEmpty()) {
            data.enemySpawns.add(new Point(0, 0));
            data.enemySpawns.add(new Point(12 * CELL, 0));
            data.enemySpawns.add(new Point(24 * CELL, 0));
        }
        return data;
    }

    public static void applyToPanel(LevelData data, GamePanel panel) {
        panel.getWalls().clear();
        panel.getWalls().addAll(data.walls);
        panel.setEagle(data.eagle);
        panel.setPlayerSpawn(data.playerSpawn);
        panel.setEnemySpawnPoints(data.enemySpawns);
        panel.spawnPlayer();
    }

    private static LevelData buildDefaultLevel(int level) {
        ArrayList<String> lines = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < COLS; c++) {
                if (r == 0 || r == ROWS - 1 || c == 0 || c == COLS - 1) {
                    sb.append('B');
                } else if (r == 34 && c >= 22 && c <= 26) {
                    sb.append('E');
                } else if (r == 32 && c >= 20 && c <= 28) {
                    sb.append('B');
                } else if (level >= 2 && r == 16 && c % 8 == 0) {
                    sb.append('S');
                } else if (level >= 3 && r == 10 && c > 10 && c < 40) {
                    sb.append('G');
                } else {
                    sb.append('.');
                }
            }
            lines.add(sb.toString());
        }
        lines.set(32, lines.get(32).substring(0, 20) + "BBBBBBBBBB" + lines.get(32).substring(30));
        LevelData data = parseLines(lines);
        data.playerSpawn = new Point(4 * CELL, 16 * CELL);
        if (data.eagle == null) {
            data.eagle = new Eagle(12 * CELL, 17 * CELL);
        }
        return data;
    }
}
