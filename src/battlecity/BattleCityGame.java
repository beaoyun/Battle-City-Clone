package battlecity;

import battlecity.editor.MapEditorLegendPanel;
import battlecity.editor.MapEditorPanel;
import battlecity.level.LevelLoader;
import battlecity.score.HighScoreManager;
import java.awt.*;
import java.io.File;
import javax.swing.*;

public class BattleCityGame extends JFrame {

    private static final String SCORES_FILE = "scores.csv";

    // Card names for the CardLayout switcher
    private static final String CARD_SPLASH = "SPLASH";
    private static final String CARD_GAME   = "GAME";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final SplashPanel splashPanel;
    private final GamePanel gamePanel;
    private final JButton pauseButton;
    private final HighScoreManager highScores = new HighScoreManager(SCORES_FILE);
    private final MusicPlayer music = new MusicPlayer("music.wav");

    public BattleCityGame() {
        super("Battle City Clone — Term Project");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        setJMenuBar(buildMenuBar());

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        toolbar.setBackground(new Color(50, 50, 50));
        pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        pauseButton.addActionListener(e -> togglePause());
        toolbar.add(pauseButton);

        // Game panel
        gamePanel = new GamePanel();
        gamePanel.setOnGameOver(this::onGameOver);

        // Game card: toolbar on top, game panel below
        JPanel gameCard = new JPanel(new BorderLayout());
        gameCard.add(toolbar,   BorderLayout.NORTH);
        gameCard.add(gamePanel, BorderLayout.CENTER);

        // Splash
        splashPanel = new SplashPanel(this::startGameFromSplash);

        // ---- Wire up card container ----
        cards.add(splashPanel, CARD_SPLASH);
        cards.add(gameCard,    CARD_GAME);

        setLayout(new BorderLayout());
        add(cards, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        // Start on the splash screen and begin background music
        showSplash();
        music.start();
    }

    // Game transitionss
    private void showSplash() {
        cardLayout.show(cards, CARD_SPLASH);
        SwingUtilities.invokeLater(splashPanel::requestFocusInWindow);
    }

    private void startGameFromSplash() {
        cardLayout.show(cards, CARD_GAME);
        gamePanel.startNewGame();
        pauseButton.setText("Pause");
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }

    
    // Menu bar
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(makeItem("New Game",    e -> onNewGame()));
        gameMenu.add(makeItem("Map Editor",  e -> onMapEditor()));
        gameMenu.add(makeItem("Options",     e -> onOptions()));
        gameMenu.addSeparator();
        gameMenu.add(makeItem("Exit",        e -> System.exit(0)));

        JMenu helpMenu = new JMenu("View");
        helpMenu.add(makeItem("Help",  e -> onHelp()));
        helpMenu.add(makeItem("About", e -> onAbout()));
        helpMenu.add(makeItem("High Scores", e -> onHighScores()));

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JMenuItem makeItem(String label, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(listener);
        return item;
    }


    // Menu actions

    private void onNewGame() {
        // Stops any running game first
        gamePanel.stopGame();
        pauseButton.setText("Pause");
        showSplash();
    }

    private void onMapEditor() {
        JDialog dialog = new JDialog(this, "Map Editor", true);
        MapEditorPanel editor = new MapEditorPanel();
        MapEditorLegendPanel legend = new MapEditorLegendPanel();
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(
                LevelLoader.COLS * LevelLoader.CELL + 20, LevelLoader.ROWS * LevelLoader.CELL + 40));

        JPanel editorArea = new JPanel(new BorderLayout(0, 0));
        editorArea.add(scroll, BorderLayout.CENTER);
        editorArea.add(legend, BorderLayout.EAST);
        editorArea.setPreferredSize(new Dimension(
                LevelLoader.COLS * LevelLoader.CELL + MapEditorLegendPanel.WIDTH + 20, LevelLoader.ROWS * LevelLoader.CELL + 40));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton saveBtn  = new JButton("Save");
        JButton loadBtn  = new JButton("Load");
        JButton playBtn  = new JButton("▶ Play This Map");
        JButton clearBtn = new JButton("Clear");
        JButton closeBtn = new JButton("Close");

        playBtn.setFont(new Font("Monospaced", Font.BOLD, 12));
        playBtn.setForeground(new Color(0, 140, 0));

        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new File("maps"));
            chooser.setSelectedFile(new File("maps/custom.map"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    editor.saveToFile(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(dialog, "Map saved.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Save failed: " + ex.getMessage());
                }
            }
        });

        loadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new File("maps"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    editor.loadFromFile(chooser.getSelectedFile());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Load failed: " + ex.getMessage());
                }
            }
        });

        playBtn.addActionListener(e -> {
            LevelLoader.LevelData data = editor.toLevel();
            dialog.dispose();
            cardLayout.show(cards, CARD_GAME);
            gamePanel.startCustomGame(data);
            pauseButton.setText("Pause");
            SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
        });

        clearBtn.addActionListener(e -> editor.clearGrid());
        closeBtn.addActionListener(e -> dialog.dispose());

        buttons.add(saveBtn);
        buttons.add(loadBtn);
        buttons.add(playBtn);
        buttons.add(clearBtn);
        buttons.add(closeBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(editorArea, BorderLayout.CENTER);
        dialog.add(buttons,  BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void onOptions() {
        String[] choices = { "Music: ON", "Music: OFF" };
        String current = music.isMuted() ? choices[1] : choices[0];
        String selected = (String) JOptionPane.showInputDialog(this,
                "Game options", "Options",
                JOptionPane.PLAIN_MESSAGE, null, choices, current);
        if (selected != null) {
            music.setMuted(selected.equals("Music: OFF"));
        }
    }

    private void onHighScores() {
        highScores.reload();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-14s %8s %12s %8s%n", "Name", "Time(s)", "Date", "Score"));
        sb.append("─".repeat(46)).append("\n");
        int count = Math.min(10, highScores.getEntries().size());
        for (int i = 0; i < count; i++) {
            HighScoreManager.Entry e = highScores.getEntries().get(i);
            sb.append(String.format("%-14s %8d %12s %8d%n", e.name, e.timeSeconds, e.date, e.score));
        }
        if (count == 0) sb.append("(no scores yet)");
        JTextArea area = new JTextArea(sb.toString(), 14, 48);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),"High Scores - Top 10", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onHelp() {
        JOptionPane.showMessageDialog(this,
                "CONTROLS\n"
                + "  Move:  W A S D\n"
                + "  Fire:  SPACE\n"
                + "  Pause:  P or the Pause button\n\n"
                + "GOAL\n"
                + "  Destroy 20 enemy tanks per level.\n"
                + "  Protect the Eagle at the bottom!\n\n"
                + "POWER-UPS\n"
                + "  Tank:  extra life\n"
                + "  Star:  faster shots / 2 shots / destroy steel\n"
                + "  Bomb:  destroy all on-screen enemies\n"
                + "  Clock:  freeze enemies temporarily\n"
                + "  Shovel:  steel walls around base temporarily\n"
                + "  Shield:  invulnerability temporarily",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAbout() {
        JOptionPane.showMessageDialog(this,
                "Battle City Clone - CSE212 Term Project\n\n"
                + "Name: Bora Erdem\n"
                + "Surname: Alan\n"
                + "School Number: 20240702042\n"
                + "Personal E-mail: boraerdemalan@gmail.com\n"
                + "University E-mail: boraerdem.alan@std.yeditepe.edu.tr\n\n"
                + "Yeditepe University — Spring 2026",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onGameOver() {
        boolean won = gamePanel.isVictory();
        String prompt  = won ? "You win!\nEnter your name for the high score board:" : "Game Over!\nEnter your name for the high score board:";
        String title   = won ? "You win!" : "Game Over";
        String summary = won ? "You cleared all levels!\nFinal score: " + gamePanel.getScore() : "Final score: " + gamePanel.getScore();

        String name = JOptionPane.showInputDialog(this, prompt, title, JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) name = "Player";
        highScores.add(name.trim(), gamePanel.getElapsedSeconds(), gamePanel.getScore());
        JOptionPane.showMessageDialog(this, summary, title, JOptionPane.INFORMATION_MESSAGE);
        showSplash();
    }

    private void togglePause() {
        boolean nowPaused = gamePanel.togglePause();
        pauseButton.setText(nowPaused ? "Resume" : "Pause");
        gamePanel.requestFocusInWindow();
    }

    // Entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BattleCityGame game = new BattleCityGame();
            game.setVisible(true);
        });
    }
}
