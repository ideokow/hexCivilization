package model.menu;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * manage permanent background music
 */
public class MusicManager {

    private Clip clip;
    private FloatControl volumeControl;
    private float currentVolume = 0.5f;

    public void loadAndPlay(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Grab the gain control so we can adjust volume later
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(currentVolume);
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("[Error]: Could not load background music: " + e.getMessage());
        }
    }

    public void setVolume(float value) {

        if (volumeControl == null) return;

        // set volume
        // ignore >1 and <0 values
        this.currentVolume = Math.max(0f, Math.min(1f, value));

        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();

        float gain;
        if (currentVolume == 0f) {
            gain = min;
        } else {
            gain = (float) (Math.log10(currentVolume) * 20.0);
            gain = Math.max(min, Math.min(max, gain));
        }

        volumeControl.setValue(gain);
    }

    public float getVolume() {
        return currentVolume;
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
