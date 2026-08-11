package view.game;

import model.game.season.SeasonName;
import model.game.townhall.TownHall;
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

    private static final String[] HAPPINESS_EMOJIS = {
            "\uD83D\uDC4D",  // Golden Age
            "\uD83D\uDE10",  // Normal
            "\uD83D\uDE1E",  // Discontent
            "\uD83D\uDD25"   // Rebellion
    };

    private static final String[] SEASON_EMOJIS = {
            "\uD83C\uDF38",   // SPRING
            "\u2600\uFE0F",   // SUMMER
            "\uD83C\uDF42",   // FALL
            "\u2744\uFE0F"    // WINTER
    };

    private final GameViewModel viewModel;
    private final JLabel turnLabel;
    private final JLabel seasonLabel;
    private final JLabel unitsLabel;
    private final JLabel unitBreakdownLabel;
    private final JLabel happinessLabel;
    private final JLabel eraLabel;
    private final Map<Resource, JLabel> resourceLabels;

    GameHudPanel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.turnLabel = new JLabel();
        this.seasonLabel = new JLabel();
        this.unitsLabel = new JLabel();
        this.unitBreakdownLabel = new JLabel();
        this.happinessLabel = new JLabel();
        this.eraLabel = new JLabel();
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
                seasonLabel,
                new Color(238, 241, 247),
                Font.BOLD,
                14
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
        configureLabel(
                happinessLabel,
                new Color(238, 241, 247),
                Font.BOLD,
                14
        );
        configureLabel(
                eraLabel,
                new Color(188, 198, 215),
                Font.PLAIN,
                12
        );

        panel.add(turnLabel);
        panel.add(seasonLabel);
        panel.add(happinessLabel);
        panel.add(eraLabel);
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
        seasonLabel.setText(
                seasonLabelText(viewModel.getSeason())
        );
        happinessLabel.setText(
                "Happiness " + townHall.getHappiness().getValue()
        );
        eraLabel.setText(
                eraLabelText(townHall)
        );
        unitsLabel.setText(
                "Units "
                        + townHall.getUnitNumber()
                        + "/"
                        + townHall.getUnitCap()
        );
        unitBreakdownLabel.setText(buildUnitBreakdown());

        refreshResources(townHall);
    }

    private String eraLabelText(TownHall townHall) {
        model.game.happiness.Era era = townHall.getHappiness().getEra();
        String emoji = HAPPINESS_EMOJIS[era.ordinal()];
        return emoji + " " + ViewTextFormatter.pretty(era);
    }

    private String seasonLabelText(SeasonName season) {
        String emoji = SEASON_EMOJIS[season.ordinal()];
        return emoji + " " + ViewTextFormatter.pretty(season);
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
