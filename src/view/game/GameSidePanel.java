package view.game;

import model.game.building.BuildingType;
import model.game.building.Upgrade;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.unit.Builder;
import model.game.unit.Unit;
import model.game.unit.Worker;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;

final class GameSidePanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(17, 23, 33);
    private static final Color CARD_BACKGROUND = new Color(27, 35, 49);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color TEXT_PRIMARY = new Color(234, 238, 245);
    private static final Color TEXT_MUTED = new Color(157, 171, 191);
    private static final Color ACCENT = new Color(238, 190, 78);
    private static final Color ACTION_BLUE = new Color(52, 91, 132);
    private static final Color ACTION_GREEN = new Color(52, 111, 89);

    private final GameViewModel viewModel;
    private final GameViewState viewState;
    private final Runnable unitSelectionChangedHandler;

    private final JTextArea selectedInfoArea;
    private final JTextArea townHallQueueArea;
    private final DefaultComboBoxModel<Unit> selectedUnitModel;
    private final JComboBox<Unit> selectedUnitCombo;
    private final JComboBox<BuildingType> buildingCombo;
    private final JComboBox<Upgrade> upgradeCombo;
    private final JComboBox<UnitType> unitTypeCombo;
    private final JButton buildButton;
    private final JButton upgradeButton;
    private final JButton generateUnitButton;
    private final JButton stationButton;
    private final JButton expandButton;
    private final JButton routeButton;
    private final JButton clearRouteButton;
    private final JButton endTurnButton;
    private final JButton resetCameraButton;

    private boolean updatingUnitCombo;

    GameSidePanel(
            GameViewModel viewModel,
            GameViewState viewState,
            Runnable unitSelectionChangedHandler
    ) {
        this.viewModel = viewModel;
        this.viewState = viewState;
        this.unitSelectionChangedHandler =
                unitSelectionChangedHandler;

        this.selectedInfoArea = new JTextArea();
        this.townHallQueueArea = new JTextArea();
        this.selectedUnitModel = new DefaultComboBoxModel<>();
        this.selectedUnitCombo =
                new JComboBox<>(selectedUnitModel);

        this.buildingCombo = new JComboBox<>(
                new BuildingType[]{
                        BuildingType.LUMBER_MILL,
                        BuildingType.STONE_MINE,
                        BuildingType.IRON_MINE,
                        BuildingType.FARM,
                        BuildingType.STABLE,
                        BuildingType.VILLAGE,
                        BuildingType.TOWN
                }
        );
        this.upgradeCombo = new JComboBox<>(Upgrade.values());
        this.unitTypeCombo = new JComboBox<>(UnitType.values());

        this.buildButton = new JButton("Build");
        this.upgradeButton = new JButton("Start Upgrade");
        this.generateUnitButton = new JButton("Generate Unit");
        this.stationButton = new JButton("Station Worker");
        this.expandButton = new JButton("Expand Territory");
        this.routeButton = new JButton("Set Route");
        this.clearRouteButton = new JButton("Clear Route");
        this.endTurnButton = new JButton("End Turn");
        this.resetCameraButton = new JButton("Reset Camera");

        configurePanel();
        buildContent();
        configureListeners();
    }

    private void configurePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(310, 100));
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                1,
                                0,
                                0,
                                new Color(54, 67, 86)
                        ),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                )
        );
        setBackground(PANEL_BACKGROUND);
    }

    private void buildContent() {
        add(createTitle("Selection"));
        add(Box.createVerticalStrut(8));

        configureSelectedInfoArea();
        add(selectedInfoArea);
        add(Box.createVerticalStrut(12));

        add(createTitle("Town Hall Queue"));
        add(Box.createVerticalStrut(8));
        configureTownHallQueueArea();
        add(townHallQueueArea);
        add(Box.createVerticalStrut(8));

        configureUpgradeCombo();
        add(upgradeCombo);
        add(Box.createVerticalStrut(6));
        addActionButton(upgradeButton, 34);
        add(Box.createVerticalStrut(6));

        configureUnitTypeCombo();
        add(unitTypeCombo);
        add(Box.createVerticalStrut(6));
        addActionButton(generateUnitButton, 34);
        add(Box.createVerticalStrut(16));

        add(createSectionLabel("Unit on selected hex"));
        add(Box.createVerticalStrut(4));

        configureUnitCombo();
        add(selectedUnitCombo);
        add(Box.createVerticalStrut(16));

        add(createTitle("Actions"));
        add(Box.createVerticalStrut(8));

        configureBuildingCombo();
        add(buildingCombo);
        add(Box.createVerticalStrut(6));

        addActionButton(buildButton, 34);
        add(Box.createVerticalStrut(6));

        addActionButton(stationButton, 34);
        add(Box.createVerticalStrut(6));

        addActionButton(expandButton, 34);
        add(Box.createVerticalStrut(6));

        addActionButton(routeButton, 34);
        add(Box.createVerticalStrut(6));

        addActionButton(clearRouteButton, 34);
        add(Box.createVerticalStrut(6));

        addActionButton(endTurnButton, 38);
        add(Box.createVerticalStrut(6));

        addActionButton(resetCameraButton, 34);
        add(Box.createVerticalStrut(18));

        add(createTitle("Legend"));
        add(Box.createVerticalStrut(8));
        add(createLegendArea());

        clearRouteButton.setVisible(false);
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(ACCENT);
        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 0, new Color(67, 78, 95)
                        ),
                        BorderFactory.createEmptyBorder(0, 0, 6, 0)
                )
        );
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void configureSelectedInfoArea() {
        selectedInfoArea.setEditable(false);
        selectedInfoArea.setOpaque(true);
        selectedInfoArea.setBackground(CARD_BACKGROUND);
        selectedInfoArea.setForeground(TEXT_PRIMARY);
        selectedInfoArea.setCaretColor(TEXT_PRIMARY);
        selectedInfoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        selectedInfoArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(47, 61, 80)),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10)
                )
        );
        selectedInfoArea.setLineWrap(true);
        selectedInfoArea.setWrapStyleWord(true);
        selectedInfoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureUnitCombo() {
        styleComboBox(selectedUnitCombo);
        selectedUnitCombo.setRenderer(new UnitRenderer());
    }

    private void configureTownHallQueueArea() {
        townHallQueueArea.setEditable(false);
        townHallQueueArea.setOpaque(true);
        townHallQueueArea.setBackground(CARD_BACKGROUND);
        townHallQueueArea.setForeground(TEXT_PRIMARY);
        townHallQueueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        townHallQueueArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(47, 61, 80)),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10)
                )
        );
        townHallQueueArea.setLineWrap(true);
        townHallQueueArea.setWrapStyleWord(true);
        townHallQueueArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureUpgradeCombo() {
        styleComboBox(upgradeCombo);
    }

    private void configureUnitTypeCombo() {
        styleComboBox(unitTypeCombo);
    }

    private void configureBuildingCombo() {
        styleComboBox(buildingCombo);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setUI(new DarkComboBoxUI());

        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 32)
        );
        comboBox.setPreferredSize(
                new Dimension(280, 32)
        );

        comboBox.setBackground(CONTROL_BACKGROUND);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        comboBox.setOpaque(true);
        comboBox.setFocusable(false);

        comboBox.setBorder(
                BorderFactory.createLineBorder(
                        new Color(67, 83, 105)
                )
        );

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                false
                        );

                label.setOpaque(true);
                label.setForeground(
                        comboBox.isEnabled()
                                ? TEXT_PRIMARY
                                : TEXT_MUTED
                );
                label.setBackground(
                        isSelected
                                ? new Color(62, 83, 108)
                                : CONTROL_BACKGROUND
                );
                label.setBorder(
                        BorderFactory.createEmptyBorder(
                                4,
                                10,
                                4,
                                8
                        )
                );

                return label;
            }
        });
    }


    private void addActionButton(
            JButton button,
            int height
    ) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, height)
        );
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(
                button == endTurnButton ? ACTION_GREEN : ACTION_BLUE
        );
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(91, 118, 145)
                        ),
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)
                )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(button);
    }

    private JTextArea createLegendArea() {
        JTextArea legendArea = new JTextArea("""
                Drag: pan camera
                Mouse wheel: discrete zoom
                Gold border: territory
                Dark hex: fog of war
                Colored dots: units
                TH/LM/SM/IM/FM/ST: buildings
                """);

        legendArea.setEditable(false);
        legendArea.setOpaque(true);
        legendArea.setBackground(CARD_BACKGROUND);
        legendArea.setForeground(TEXT_MUTED);
        legendArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        legendArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(47, 61, 80)),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10)
                )
        );
        legendArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        return legendArea;
    }

    private void configureListeners() {
        selectedUnitCombo.addActionListener(event -> {
            if (updatingUnitCombo) {
                return;
            }

            Unit selectedUnit =
                    (Unit) selectedUnitCombo.getSelectedItem();

            viewState.setSelectedUnit(selectedUnit);
            unitSelectionChangedHandler.run();
        });
    }

    void refreshUnitCombo() {
        updatingUnitCombo = true;

        try {
            selectedUnitModel.removeAllElements();
            addUnitsFromSelectedHex();
            restoreSelectedUnit();
            selectedUnitCombo.setEnabled(
                    selectedUnitModel.getSize() > 0
            );
        } finally {
            updatingUnitCombo = false;
        }
    }

    private void addUnitsFromSelectedHex() {
        HexCoordinate selectedHex = viewState.getSelectedHex();

        if (selectedHex == null) {
            return;
        }

        Hex hex = viewModel.getHex(selectedHex);

        if (hex == null) {
            return;
        }

        for (Unit unit : hex.getUnits()) {
            selectedUnitModel.addElement(unit);
        }
    }

    private void restoreSelectedUnit() {
        Unit selectedUnit = viewState.getSelectedUnit();

        if (selectedUnit != null
                && viewModel.containsUnit(selectedUnit)
                && containsUnitInModel(selectedUnit)) {
            selectedUnitCombo.setSelectedItem(selectedUnit);
            return;
        }

        if (selectedUnitModel.getSize() > 0) {
            Unit firstUnit = selectedUnitModel.getElementAt(0);
            viewState.setSelectedUnit(firstUnit);
            selectedUnitCombo.setSelectedItem(firstUnit);
            return;
        }

        viewState.setSelectedUnit(null);
    }

    private boolean containsUnitInModel(Unit unit) {
        for (
                int index = 0;
                index < selectedUnitModel.getSize();
                index++
        ) {
            if (unit.equals(selectedUnitModel.getElementAt(index))) {
                return true;
            }
        }

        return false;
    }

    void refreshSelectionPanel() {
        HexCoordinate selectedHex = viewState.getSelectedHex();

        if (selectedHex == null) {
            selectedInfoArea.setText("No hex selected.");
            return;
        }

        Hex hex = viewModel.getHex(selectedHex);

        if (hex == null) {
            selectedInfoArea.setText(
                    "Selected hex is outside the map."
            );
            return;
        }

        selectedInfoArea.setText(
                buildSelectionText(selectedHex, hex)
        );
    }

    void refreshTownHallQueue() {
        Upgrade upgrade = viewModel.getInQueueUpgrade();
        UnitType unitType = viewModel.getInQueueUnitType();

        StringBuilder text = new StringBuilder();
        text.append("Upgrade: ");
        if (upgrade == null) {
            text.append("Empty");
        } else {
            text.append(upgrade.getName())
                    .append(" (")
                    .append(viewModel.getUpgradeRemainingTurns())
                    .append(" turns)");
        }

        text.append('\n').append("Unit: ");
        if (unitType == null) {
            text.append("Empty");
        } else {
            text.append(unitType.getName())
                    .append(" (")
                    .append(viewModel.getGenerationRemainingTurns())
                    .append(" turns)");
        }

        townHallQueueArea.setText(text.toString());
        upgradeButton.setEnabled(upgrade == null);
        generateUnitButton.setEnabled(unitType == null);
    }

    void setRouteControls(boolean unitSelected, boolean routeExists) {
        routeButton.setEnabled(unitSelected);
        routeButton.setVisible(unitSelected);
        clearRouteButton.setVisible(unitSelected && routeExists);
        revalidate();
        repaint();
    }

    private String buildSelectionText(
            HexCoordinate selectedHex,
            Hex hex
    ) {
        StringBuilder text = new StringBuilder();
        boolean discovered =
                viewModel.isDiscovered(selectedHex);

        text.append("Hex: ")
                .append(
                        ViewTextFormatter.formatCoordinate(
                                selectedHex
                        )
                )
                .append('\n');

        text.append("Discovered: ")
                .append(discovered ? "Yes" : "No")
                .append('\n');

        text.append("Territory: ")
                .append(
                        viewModel.ownsTerritory(selectedHex)
                                ? "Yes"
                                : "No"
                )
                .append('\n');

        if (discovered) {
            appendDiscoveredHexInformation(text, hex);
        } else {
            text.append("Terrain: Unknown\n");
            text.append("Resources: Hidden by fog\n");
        }

        appendSelectedUnitInformation(text);
        return text.toString();
    }

    private void appendDiscoveredHexInformation(
            StringBuilder text,
            Hex hex
    ) {
        text.append("Terrain: ")
                .append(
                        ViewTextFormatter.pretty(
                                hex.getTerrain()
                        )
                )
                .append('\n');

        text.append("Move AP: ")
                .append(hex.getTerrain().getMovementCost())
                .append('\n');

        text.append("Resources: ")
                .append(
                        ViewTextFormatter.formatResources(
                                hex.getAvailableResources()
                        )
                )
                .append('\n');

        text.append("Building: ")
                .append(
                        ViewTextFormatter.formatBuilding(
                                hex.getBuilding()
                        )
                )
                .append('\n');

        text.append("Units: ")
                .append(hex.getUnits().size())
                .append('\n');
    }

    private void appendSelectedUnitInformation(
            StringBuilder text
    ) {
        Unit selectedUnit = viewState.getSelectedUnit();

        if (selectedUnit == null) {
            return;
        }

        text.append('\n');
        text.append("Selected Unit\n");

        text.append("Type: ")
                .append(
                        ViewTextFormatter.pretty(
                                selectedUnit.getType()
                        )
                )
                .append('\n');

        text.append("AP: ")
                .append(selectedUnit.getCurrentAP())
                .append('/')
                .append(
                        selectedUnit
                                .getType()
                                .getEachTurnAP()
                )
                .append('\n');

        text.append("Vision: ")
                .append(
                        selectedUnit
                                .getType()
                                .getVisibilityRadius()
                )
                .append('\n');

        if (selectedUnit instanceof Builder builder) {
            text.append("Charges: ")
                    .append(builder.getCharges())
                    .append('\n');
        }

        if (selectedUnit instanceof Worker worker) {
            text.append("Stationed: ")
                    .append(
                            worker.isInBuilding()
                                    ? "Yes"
                                    : "No"
                    )
                    .append('\n');
        }
    }

    JButton getBuildButton() {
        return buildButton;
    }

    JButton getUpgradeButton() {
        return upgradeButton;
    }

    JButton getGenerateUnitButton() {
        return generateUnitButton;
    }

    JButton getStationButton() {
        return stationButton;
    }

    JButton getExpandButton() {
        return expandButton;
    }

    JButton getRouteButton() {
        return routeButton;
    }

    JButton getClearRouteButton() {
        return clearRouteButton;
    }

    JButton getEndTurnButton() {
        return endTurnButton;
    }

    JButton getResetCameraButton() {
        return resetCameraButton;
    }

    BuildingType getSelectedBuildingType() {
        return (BuildingType) buildingCombo.getSelectedItem();
    }

    Upgrade getSelectedUpgrade() {
        return (Upgrade) upgradeCombo.getSelectedItem();
    }

    UnitType getSelectedUnitType() {
        return (UnitType) unitTypeCombo.getSelectedItem();
    }
}
