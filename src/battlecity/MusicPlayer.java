package battlecity;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class MusicPlayer {

    private Clip clip;
    private boolean muted = false;

    public MusicPlayer(String filename) {
        String[] paths = {filename, "../" + filename, "BattleCity/" + filename};
        for (String p : paths) {
            File f = new File(p);
            if (!f.exists()) continue;
            try {
                AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                clip = AudioSystem.getClip();
                clip.open(ais);
                return;
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                System.err.println("MusicPlayer: could not load " + p + " — " + ex.getMessage());
            }
        }
        System.err.println("MusicPlayer: " + filename + " not found.");
    }

    public void start() {
        if (clip == null || clip.isRunning()) return;
        clip.setFramePosition(0);
        if (!muted) clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void setMuted(boolean mute) {
        muted = mute;
        if (clip == null) return;
        if (muted) {
            clip.stop();
        } else {
            if (!clip.isRunning()) {clip.loop(Clip.LOOP_CONTINUOUSLY);}
        }
    }

    public boolean isMuted() {return muted;}

    public void stop() {
        if (clip != null && clip.isRunning()) {clip.stop();}
    }
}
