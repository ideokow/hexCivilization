package view.game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class ToastWindow {

    private static final int DURATION_MS = 3000;
    private static final int BOTTOM_MARGIN = 80;
    private static final int TOAST_GAP = 8;

    private final JFrame owner;
    private final List<Toast> toasts = new ArrayList<>();

    ToastWindow(JFrame owner) {
        this.owner = owner;
    }

    void show(String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> show(message));
            return;
        }

        Toast toast = new Toast(message);
        toasts.add(toast);
        updateWindowLocations();
        toast.window.setVisible(true);
        toast.timer.start();
    }

    private void updateWindowLocations() {
        Point ownerLocation = owner.getLocation();
        int nextY = ownerLocation.y + owner.getHeight() - BOTTOM_MARGIN;

        for (int index = toasts.size() - 1; index >= 0; index--) {
            JWindow window = toasts.get(index).window;
            int x = ownerLocation.x + (owner.getWidth() - window.getWidth()) / 2;
            int y = nextY - window.getHeight();

            window.setLocation(x, y);
            nextY = y - TOAST_GAP;
        }
    }

    private void remove(Toast toast) {
        toasts.remove(toast);
        toast.window.dispose();
        updateWindowLocations();
    }

    private final class Toast {

        private final JWindow window;
        private final Timer timer;

        private Toast(String message) {
            window = new JWindow(owner);

            JLabel label = new JLabel(message, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(new Color(33, 40, 56));
            label.setForeground(new Color(238, 241, 247));
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            window.getContentPane().add(label);
            window.setFocusableWindowState(false);
            window.pack();

            timer = new Timer(DURATION_MS, event -> remove(this));
            timer.setRepeats(false);
        }
    }
}
