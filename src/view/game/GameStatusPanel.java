package view.game;

import javax.swing.*;
import java.awt.*;

final class GameStatusPanel extends JPanel {

    private static final String DEFAULT_STATUS =
            "Select a hex. Select a unit, then click "
                    + "an adjacent discovered hex to move.";

    private final JLabel statusLabel;
    private final JLabel alertLabel;

    GameStatusPanel() {
        this.statusLabel = new JLabel(DEFAULT_STATUS);
        this.alertLabel = new JLabel();

        configurePanel();
        buildContent();
    }

    private void configurePanel() {
        setLayout(new BorderLayout());
        setBorder(
                BorderFactory.createEmptyBorder(6, 12, 8, 12)
        );
        setBackground(new Color(22, 27, 36));
    }

    private void buildContent() {
        statusLabel.setForeground(new Color(228, 232, 240));
        statusLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        alertLabel.setForeground(new Color(211, 63, 73));
        alertLabel.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        add(statusLabel, BorderLayout.CENTER);
        add(alertLabel, BorderLayout.EAST);
    }

    void setStatus(String message) {
        statusLabel.setText(message);
    }

    void setAlert(String message) {
        alertLabel.setText(message == null ? "" : message);
    }
}