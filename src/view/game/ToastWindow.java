package view.game;

import javax.swing.*;
import java.awt.*;

final class ToastWindow {

    private static final int DURATION_MS = 3000;

    private final JWindow window;
    private final JLabel label;
    private final JFrame owner;

    private Timer timer;

    ToastWindow(JFrame owner) {
        this.owner = owner;
        this.window = new JWindow(owner);
        this.label = new JLabel(
                " ",
                SwingConstants.CENTER
        );

        configureWindow();
    }

    private void configureWindow() {
        label.setOpaque(true);
        label.setBackground(new Color(33, 40, 56));
        label.setForeground(new Color(238, 241, 247));
        label.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );
        label.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        16,
                        8,
                        16
                )
        );

        window.getContentPane().add(label);
        window.setFocusableWindowState(false);
    }

    void show(String message) {
        label.setText(message);
        window.pack();

        updateWindowLocation();
        window.setVisible(true);
        restartTimer();
    }

    private void updateWindowLocation() {
        Point ownerLocation = owner.getLocation();

        int x = ownerLocation.x
                + (owner.getWidth() - window.getWidth()) / 2;

        int y = ownerLocation.y
                + owner.getHeight()
                - window.getHeight()
                - 80;

        window.setLocation(x, y);
    }

    private void restartTimer() {
        if (timer != null && timer.isRunning()) {
            timer.restart();
            return;
        }

        timer = new Timer(
                DURATION_MS,
                event -> window.setVisible(false)
        );
        timer.setRepeats(false);
        timer.start();
    }
}