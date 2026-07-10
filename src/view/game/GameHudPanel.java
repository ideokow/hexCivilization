package view.game;

import model.game.building.TownHall;
import model.game.hex.Resource;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class GameHudPanel extends JPanel {

    private static final Resource[] STORED_RESOURCES = {
            Resource.FOOD,
            Resource.WOOD,
            Resource.STONE,
            Resource.IRON
    };

    private final GameViewModel viewModel;
    private final JLabel turnLabel;
    private final JLabel unitsLabel;
    private final JLabel unitBreakdownLabel;
    private final Map<Resource, JLabel> resourceLabels;

    GameHudPanel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.turnLabel = new JLabel();
        this.unitsLabel = new JLabel();
        this.unitBreakdownLabel = new JLabel();
        this.resourceLabels = new EnumMap<>(Resource.class);

        configurePanel();
        buildContent();
    }

    private void configurePanel() {
        setLayout(new BorderLayout(10, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        setBackground(new Color(28, 35, 48));
    }

    private void buildContent() {
        add(buildStatisticsPanel(), BorderLayout.WEST);
        add(buildResourcesPanel(), BorderLayout.EAST);
    }

    private JPanel buildStatisticsPanel() {
        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0)
        );
        panel.setOpaque(false);

        configureLabel(
                turnLabel,
                new Color(238, 241, 247),
                Font.BOLD,
                16
        );
        configureLabel(
                unitsLabel,
                new Color(238, 241, 247),
                Font.BOLD,
                14
        );
        configureLabel(
                unitBreakdownLabel,
                new Color(188, 198, 215),
                Font.PLAIN,
                12
        );

        panel.add(turnLabel);
        panel.add(unitsLabel);
        panel.add(unitBreakdownLabel);

        return panel;
    }

    private JPanel buildResourcesPanel() {
        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        panel.setOpaque(false);

        for (Resource resource : STORED_RESOURCES) {
            JLabel resourceLabel = new JLabel();
            resourceLabel.setFont(
                    new Font("SansSerif", Font.BOLD, 13)
            );

            resourceLabels.put(resource, resourceLabel);
            panel.add(resourceLabel);
        }

        return panel;
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
        TownHall townHall = viewModel.getTownHall();

        turnLabel.setText("Turn " + viewModel.getTurnNumber());
        unitsLabel.setText(
                "Units "
                        + townHall.getUnitNumber()
                        + "/"
                        + townHall.getUnitCap()
        );
        unitBreakdownLabel.setText(buildUnitBreakdown());

        refreshResources(townHall);
    }

    private void refreshResources(TownHall townHall) {
        Map<Resource, Integer> storage =
                townHall.getResourceStorage();

        for (Resource resource : STORED_RESOURCES) {
            int amount = storage.getOrDefault(resource, 0);
            int net = viewModel.getNetResource(resource);

            String netText =
                    (net >= 0 ? "+" : "") + net + "/turn";
            String netColor =
                    net < 0 ? "#d33f49" : "#3cb371";

            JLabel label = resourceLabels.get(resource);
            label.setText(
                    "<html>"
                            + "<span style='color:#eef1f7'>"
                            + resource.getDisplayName()
                            + " "
                            + amount
                            + "/"
                            + townHall.getResourceCap()
                            + "</span> "
                            + "<span style='color:"
                            + netColor
                            + "'>"
                            + netText
                            + "</span>"
                            + "</html>"
            );
        }
    }

    private String buildUnitBreakdown() {
        Map<UnitType, Integer> counts =
                new EnumMap<>(UnitType.class);

        for (UnitType unitType : UnitType.values()) {
            counts.put(unitType, 0);
        }

        for (Unit unit : viewModel.getUnits()) {
            UnitType unitType = unit.getType();
            counts.put(unitType, counts.get(unitType) + 1);
        }

        List<String> parts = new ArrayList<>();

        for (UnitType unitType : UnitType.values()) {
            int count = counts.get(unitType);

            if (count > 0) {
                parts.add(
                        ViewTextFormatter.pretty(unitType)
                                + " "
                                + count
                );
            }
        }

        return String.join("  |  ", parts);
    }
}