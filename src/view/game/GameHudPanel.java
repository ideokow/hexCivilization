package view.game;

import javax.swing.*;
import java.awt.*;

final class GameHudPanel extends JPanel {

    private final GameViewModel viewModel;
    private final JLabel turnLabel;

    GameHudPanel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.turnLabel = new JLabel();

        configurePanel();
        buildContent();
    }

    private void configurePanel() {
        setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        setBackground(new Color(28, 35, 48));
    }

    private void buildContent() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        configureLabel(turnLabel, new Color(238, 241, 247), Font.BOLD, 16);
        add(turnLabel);
    }

    private void configureLabel(
            JLabel label,
            Color color,
            int style,
            int size
    ) {
        label.setForeground(color);
        label.setFont(new Font("SansSerif", style, size));
    }

    void refresh() {
        turnLabel.setText("Turn " + viewModel.getTurnNumber());
    }
}
