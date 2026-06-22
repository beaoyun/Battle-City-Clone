package battlecity.score;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

// Loads and saves high scores to a CSV file
public class HighScoreManager {

    public static class Entry {
        public final String name;
        public final long timeSeconds;
        public final String date;
        public final int score;

        public Entry(String name, long timeSeconds, String date, int score) {
            this.name = name; this.timeSeconds = timeSeconds;
            this.date = date; this.score = score;
        }

        public String toCsv() {
            return escape(name) + "," + timeSeconds + "," + escape(date) + "," + score;
        }

        private static String escape(String s) {
            return s.replace(",", " ");   // trivial CSV, no embedded commas
        }
    }

    private final File file;
    private final ArrayList<Entry> entries = new ArrayList<>();

    public HighScoreManager(String filename) {
        this.file = new File(filename);
        load();
    }

    public ArrayList<Entry> getEntries() {return entries;}

    public void reload() { load(); }

    public void add(String playerName, long timeSeconds, int score) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        entries.add(new Entry(playerName, timeSeconds, date, score));
        Collections.sort(entries, new Comparator<Entry>() {
            @Override public int compare(Entry a, Entry b) {
                return Integer.compare(b.score, a.score);   // descending
            }
        });
        save();
    }

    public void load() {
        entries.clear();
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length != 4) continue;
                entries.add(new Entry(
                        parts[0],
                        Long.parseLong(parts[1].trim()),
                        parts[2],
                        Integer.parseInt(parts[3].trim())));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Could not load high scores: " + ex.getMessage());
        }
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (Entry e : entries) pw.println(e.toCsv());
        } catch (IOException ex) {
            System.err.println("Could not save high scores: " + ex.getMessage());
        }
    }
}
