package view.menu;

import javax.swing.*;
import java.awt.*;

/**
 * simple settings page template (music slider)
 * no logic
 */
public class SettingsView extends JDialog {

    private final JSlider volumeSlider;
    private final JButton backButton;

    public SettingsView(Frame owner, int initialVolume) {
        super(owner, "Settings", true);
        setSize(400, 200);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // title
        JLabel title = new JLabel("Music Volume", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
        add(title, BorderLayout.NORTH);

        // slider
        volumeSlider = new JSlider(0, 100, initialVolume);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(volumeSlider, BorderLayout.CENTER);

        // back button
        backButton = new JButton("Back");
        JPanel bottom = new JPanel();
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public JSlider getVolumeSlider() {
        return volumeSlider;
    }

    public JButton getBackButton() {
        return backButton;
    }
}
