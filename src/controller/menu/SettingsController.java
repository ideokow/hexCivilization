package controller.menu;

import model.menu.MusicManager;
import view.menu.SettingsView;

import javax.swing.*;

/**
 * settings (music) controller
 */
public class SettingsController {

    private final SettingsView view;
    private final MusicManager musicManager;

    public SettingsController(JFrame owner, MusicManager musicManager) {
        this.musicManager = musicManager;

        // Convert current volume (0.0..1.0) to slider scale (0..100)
        int currentVolume = Math.round(musicManager.getVolume() * 100);
        this.view = new SettingsView(owner, currentVolume);

        attachListeners();
    }

    private void attachListeners() {
        view.getVolumeSlider().addChangeListener(e -> {
            int sliderValue = view.getVolumeSlider().getValue();
            musicManager.setVolume(sliderValue / 100f);
        });

        view.getBackButton().addActionListener(e -> view.dispose());
    }

    public void show() {
        view.setVisible(true);
    }
}
