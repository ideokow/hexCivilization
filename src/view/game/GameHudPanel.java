package view.game;

import model.game.season.SeasonName;

import javax.swing.*;
import java.awt.*;

final class GameHudPanel extends JPanel {

    private final GameViewModel viewModel;
    private final JLabel turnLabel;
    private final JLabel seasonLabel;

    GameHudPanel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.turnLabel = new JLabel();
        this.seasonLabel = new JLabel();

        configurePanel();
        buildContent();
    }

    private void configurePanel() {
        setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        setBackground(new Color(28, 35, 48));
    }

    private void buildContent() {
        setLayout(new BorderLayout());
        configureLabel(turnLabel, new Color(238, 241, 247), Font.BOLD, 16);
        configureLabel(seasonLabel, new Color(238, 241, 247), Font.BOLD, 16);
        seasonLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(turnLabel, BorderLayout.WEST);
        add(seasonLabel, BorderLayout.EAST);
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

        SeasonName season = viewModel.getSeason();
        seasonLabel.setText("Season: " + season.getName());
        seasonLabel.setForeground(seasonColor(season));
    }

    private Color seasonColor(SeasonName season) {
        return switch (season) {
            case SPRING -> new Color(150, 220, 160);
            case SUMMER -> new Color(255, 210, 105);
            case FALL -> new Color(230, 155, 90);
            case WINTER -> new Color(170, 215, 255);
        };
    }
}
