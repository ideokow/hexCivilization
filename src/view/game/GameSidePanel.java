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
        setPreferredSize(new Dimension(292, 100));
        setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
        setBackground(new Color(236, 239, 244));
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
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void configureSelectedInfoArea() {
        selectedInfoArea.setEditable(false);
        selectedInfoArea.setOpaque(false);
        selectedInfoArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12)
        );
        selectedInfoArea.setLineWrap(true);
        selectedInfoArea.setWrapStyleWord(true);
        selectedInfoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureUnitCombo() {
        selectedUnitCombo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );
        selectedUnitCombo.setRenderer(new UnitRenderer());
    }

    private void configureTownHallQueueArea() {
        townHallQueueArea.setEditable(false);
        townHallQueueArea.setOpaque(false);
        townHallQueueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        townHallQueueArea.setLineWrap(true);
        townHallQueueArea.setWrapStyleWord(true);
        townHallQueueArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureUpgradeCombo() {
        upgradeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private void configureUnitTypeCombo() {
        unitTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private void configureBuildingCombo() {
        buildingCombo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 30)
        );
    }

    private void addActionButton(
            JButton button,
            int height
    ) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, height)
        );
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
        legendArea.setOpaque(false);
        legendArea.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
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
