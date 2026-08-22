package view.game;

import model.game.building.BuildingType;
import model.game.building.Bazaar;
import model.game.building.MilitaryStable;
import model.game.building.ProductionBuilding;
import model.game.building.TradingPost;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Wall;
import model.game.unit.BorderExpander;
import model.game.unit.Builder;
import model.game.unit.Unit;
import model.game.unit.Worker;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryUnit;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

final class HexActionPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(8, 12, 18, 232);
    private static final Color PANEL_BORDER = new Color(190, 207, 225, 210);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color TEXT_PRIMARY = new Color(240, 244, 250);
    private static final Color TEXT_MUTED = new Color(169, 183, 202);
    private static final Color ACCENT = new Color(238, 190, 78);
    private static final Color ACTION_BLUE = new Color(52, 91, 132);
    private static final Color ACTION_RED = new Color(128, 61, 70);

    private final GameViewModel viewModel;
    private final GameViewState viewState;
    private final Runnable unitSelectionChangedHandler;

    private final JTextArea selectedInfoArea;
    private final DefaultComboBoxModel<Unit> selectedUnitModel;
    private final JComboBox<Unit> selectedUnitCombo;
    private final DefaultComboBoxModel<Wall> selectedWallModel;
    private final JComboBox<Wall> selectedWallCombo;
    private final JComboBox<BuildingType> buildingCombo;
    private final JButton buildMenuButton;
    private final JButton buildWallButton;
    private final JButton tradeButton;
    private final JButton confirmBuildButton;
    private final JButton cancelBuildButton;
    private final JButton ruinButton;
    private final JButton ruinWallButton;
    private final JButton stationButton;
    private final JButton generateMilitaryUnitButton;
    private final JButton expandButton;
    private final JButton combatButton;
    private final JButton directAttackButton;
    private final JButton closeButton;
    private final JPanel wallSection;
    private final JPanel buildSelectorPanel;

    private boolean updatingUnitCombo;
    private boolean updatingWallCombo;

    HexActionPanel(
            GameViewModel viewModel,
            GameViewState viewState,
            Runnable unitSelectionChangedHandler
    ) {
        this.viewModel = viewModel;
        this.viewState = viewState;
        this.unitSelectionChangedHandler =
                unitSelectionChangedHandler;
        this.selectedInfoArea = new JTextArea();
        this.selectedUnitModel = new DefaultComboBoxModel<>();
        this.selectedUnitCombo = new JComboBox<>(selectedUnitModel);
        this.selectedWallModel = new DefaultComboBoxModel<>();
        this.selectedWallCombo = new JComboBox<>(selectedWallModel);
        this.buildingCombo = new JComboBox<>(
                new BuildingType[]{
                        BuildingType.LUMBER_MILL,
                        BuildingType.STONE_MINE,
                        BuildingType.IRON_MINE,
                        BuildingType.FARM,
                        BuildingType.STABLE,
                        BuildingType.VILLAGE,
                        BuildingType.TOWN,
                        BuildingType.MONUMENT,
                        BuildingType.DOCK,
                        BuildingType.BAZAAR,
                        BuildingType.ROAD,
                        BuildingType.MILITARY_STABLE
                }
        );
        this.buildMenuButton = new JButton("Build");
        this.buildWallButton = new JButton("Build Wall");
        this.tradeButton = new JButton("Trade");
        this.confirmBuildButton = new JButton("Confirm Build");
        this.cancelBuildButton = new JButton("Cancel");
        this.ruinButton = new JButton("Ruin");
        this.ruinWallButton = new JButton("Ruin Wall");
        this.stationButton = new JButton("Station Worker");
        this.generateMilitaryUnitButton =
                new JButton("Generate Military Unit");
        this.expandButton = new JButton("Expand Territory");
        this.combatButton = new JButton("H2H Attack");
        this.directAttackButton = new JButton("Direct Attack");
        this.closeButton = new JButton("×");
        this.wallSection = new JPanel();
        this.buildSelectorPanel = new JPanel();

        configurePanel();
        buildContent();
        configureListeners();
        setVisible(false);
    }

    private void configurePanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(330, 490));
        setMaximumSize(new Dimension(370, 580));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics.create();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics2D.setColor(PANEL_BACKGROUND);
            graphics2D.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    18,
                    18
            );
            graphics2D.setColor(PANEL_BORDER);
            graphics2D.setStroke(new BasicStroke(1.2f));
            graphics2D.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    18,
                    18
            );
        } finally {
            graphics2D.dispose();
        }
    }

    private void buildContent() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel title = new JLabel("Selected Hex");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(title, BorderLayout.WEST);

        closeButton.setForeground(TEXT_MUTED);
        closeButton.setBackground(new Color(0, 0, 0, 0));
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.add(closeButton, BorderLayout.EAST);
        add(header);
        add(Box.createVerticalStrut(4));

        configureSelectedInfoArea();
        add(selectedInfoArea);
        add(Box.createVerticalStrut(6));

        JLabel unitLabel = new JLabel("Unit on selected hex");
        unitLabel.setForeground(TEXT_MUTED);
        unitLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(unitLabel);
        add(Box.createVerticalStrut(4));

        configureUnitCombo();
        add(selectedUnitCombo);
        add(Box.createVerticalStrut(8));

        configureWallSection();
        add(wallSection);
        add(Box.createVerticalStrut(6));

        addActionButton(buildMenuButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(buildWallButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(tradeButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(ruinButton, ACTION_RED);
        add(Box.createVerticalStrut(5));
        addActionButton(ruinWallButton, ACTION_RED);
        add(Box.createVerticalStrut(5));
        addActionButton(stationButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(generateMilitaryUnitButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(expandButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(combatButton, ACTION_RED);
        add(Box.createVerticalStrut(5));
        addActionButton(directAttackButton, ACTION_RED);

        buildSelectorPanel.setOpaque(false);
        buildSelectorPanel.setLayout(new BoxLayout(buildSelectorPanel, BoxLayout.Y_AXIS));
        buildSelectorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildSelectorPanel.setBorder(
                BorderFactory.createEmptyBorder(7, 0, 0, 0)
        );

        styleComboBox(buildingCombo);
        buildSelectorPanel.add(buildingCombo);
        buildSelectorPanel.add(Box.createVerticalStrut(5));

        JPanel buildControls = new JPanel(new GridLayout(1, 2, 5, 0));
        buildControls.setOpaque(false);
        buildControls.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildControls.add(confirmBuildButton);
        buildControls.add(cancelBuildButton);
        styleSmallButton(confirmBuildButton, ACTION_BLUE);
        styleSmallButton(cancelBuildButton, new Color(66, 76, 91));
        buildSelectorPanel.add(buildControls);
        buildSelectorPanel.setVisible(false);
        add(buildSelectorPanel);
    }

    private void configureWallSection() {
        wallSection.setOpaque(false);
        wallSection.setLayout(new BoxLayout(wallSection, BoxLayout.Y_AXIS));
        wallSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        wallSection.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                1,
                                0,
                                1,
                                0,
                                new Color(130, 148, 170, 100)
                        ),
                        BorderFactory.createEmptyBorder(6, 0, 6, 0)
                )
        );

        JLabel wallLabel = new JLabel("Wall on selected edge");
        wallLabel.setForeground(TEXT_MUTED);
        wallLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        wallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wallSection.add(wallLabel);
        wallSection.add(Box.createVerticalStrut(4));

        styleComboBox(selectedWallCombo);
        wallSection.add(selectedWallCombo);
        wallSection.setVisible(false);
    }

    private void configureSelectedInfoArea() {
        selectedInfoArea.setEditable(false);
        selectedInfoArea.setOpaque(false);
        selectedInfoArea.setBackground(new Color(0, 0, 0, 0));
        selectedInfoArea.setForeground(TEXT_PRIMARY);
        selectedInfoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        selectedInfoArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                0,
                                1,
                                0,
                                new Color(130, 148, 170, 100)
                        ),
                        BorderFactory.createEmptyBorder(2, 2, 7, 2)
                )
        );
        selectedInfoArea.setLineWrap(true);
        selectedInfoArea.setWrapStyleWord(true);
        selectedInfoArea.setRows(5);
        selectedInfoArea.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 120)
        );
        selectedInfoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureUnitCombo() {
        styleComboBox(selectedUnitCombo);
        selectedUnitCombo.setRenderer(new UnitRenderer());
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setUI(new DarkComboBoxUI());
        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 32)
        );
        comboBox.setPreferredSize(
                new Dimension(300, 32)
        );
        comboBox.setBackground(CONTROL_BACKGROUND);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboBox.setOpaque(true);
        comboBox.setFocusable(false);
        comboBox.setBorder(
                BorderFactory.createLineBorder(
                        new Color(92, 112, 136)
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
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        false
                );

                if (value instanceof Enum<?> enumValue) {
                    label.setText(ViewTextFormatter.pretty(enumValue));
                } else if (value instanceof Wall wall) {
                    label.setText(ViewTextFormatter.formatWall(wall));
                }

                label.setOpaque(true);
                label.setForeground(TEXT_PRIMARY);
                label.setBackground(
                        isSelected
                                ? new Color(62, 83, 108)
                                : CONTROL_BACKGROUND
                );
                label.setBorder(
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)
                );
                return label;
            }
        });
    }

    private void addActionButton(JButton button, Color background) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(background);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(112, 137, 164)
                        ),
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)
                )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(button);
    }

    private void styleSmallButton(JButton button, Color background) {
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(background);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createLineBorder(
                        new Color(112, 137, 164)
                )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void configureListeners() {
        closeButton.addActionListener(event -> setVisible(false));
        buildMenuButton.addActionListener(
                event -> setBuildSelectorVisible(true)
        );
        cancelBuildButton.addActionListener(
                event -> setBuildSelectorVisible(false)
        );
        confirmBuildButton.addActionListener(
                event -> setBuildSelectorVisible(false)
        );
        selectedUnitCombo.addActionListener(event -> {
            if (updatingUnitCombo) {
                return;
            }

            Unit selectedUnit =
                    (Unit) selectedUnitCombo.getSelectedItem();
            viewState.setSelectedUnit(selectedUnit);
            unitSelectionChangedHandler.run();
        });
        selectedWallCombo.addActionListener(event -> {
            if (updatingWallCombo) {
                return;
            }

            HexCoordinate selectedHex = viewState.getSelectedHex();
            refreshActionAvailability(
                    selectedHex == null ? null : viewModel.getHex(selectedHex)
            );
        });
    }

    private void setBuildSelectorVisible(boolean visible) {
        buildSelectorPanel.setVisible(visible);
        buildMenuButton.setVisible(!visible);
        revalidate();
        repaint();
    }

    void openForSelection() {
        setBuildSelectorVisible(false);
        setVisible(true);
        revalidate();
        repaint();
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
            selectedInfoArea.setText("Select a hex to see its details.");
            refreshWallCombo(null);
            refreshActionAvailability(null);
            return;
        }

        Hex hex = viewModel.getHex(selectedHex);

        if (hex == null) {
            selectedInfoArea.setText("Selected hex is outside the map.");
            refreshWallCombo(null);
            refreshActionAvailability(null);
            return;
        }

        refreshWallCombo(hex);
        selectedInfoArea.setText(
                buildSelectionText(selectedHex, hex)
        );
        refreshActionAvailability(hex);
    }

    private void refreshWallCombo(Hex hex) {
        Wall previousWall = (Wall) selectedWallCombo.getSelectedItem();
        List<Wall> walls = hex == null || !viewModel.isDiscovered(hex.getCoordinate())
                ? List.of()
                : viewModel.getWalls(hex.getCoordinate());
        walls = walls.stream()
                .sorted(Comparator.comparing(ViewTextFormatter::formatWall))
                .toList();

        updatingWallCombo = true;
        try {
            selectedWallModel.removeAllElements();
            for (Wall wall : walls) {
                selectedWallModel.addElement(wall);
            }

            if (previousWall != null && walls.contains(previousWall)) {
                selectedWallCombo.setSelectedItem(previousWall);
            } else if (!walls.isEmpty()) {
                selectedWallCombo.setSelectedIndex(0);
            }
        } finally {
            updatingWallCombo = false;
        }
    }

    private void refreshActionAvailability(Hex hex) {
        boolean hasHex = hex != null;
        Unit selectedUnit = viewState.getSelectedUnit();
        List<Wall> walls = hasHex && viewModel.isDiscovered(hex.getCoordinate())
                ? viewModel.getWalls(hex.getCoordinate())
                : List.of();
        Wall selectedWall = (Wall) selectedWallCombo.getSelectedItem();

        buildMenuButton.setEnabled(
                hasHex && selectedUnit instanceof Builder
        );
        buildWallButton.setVisible(hasHex && selectedUnit instanceof Builder);
        buildWallButton.setEnabled(
                hasHex
                        && selectedUnit instanceof Builder
                        && viewModel.isDiscovered(hex.getCoordinate())
                        && viewModel.ownsTerritory(hex.getCoordinate())
        );
        boolean tradeBuilding = hasHex
                && (hex.getBuilding() instanceof Bazaar
                || hex.getBuilding() instanceof TradingPost);
        tradeButton.setVisible(tradeBuilding);
        boolean tradeInTerritory = hasHex
                && viewModel.ownsTerritory(viewState.getSelectedHex());
        boolean tradeAlreadyUsed =
                (hex != null
                        && hex.getBuilding() instanceof Bazaar
                        && viewModel.hasTradedAtBazaarThisTurn())
                || (hex != null
                        && hex.getBuilding() instanceof TradingPost
                        && viewModel.hasTradedAtTradingPostThisTurn());
        boolean tradeAvailable = tradeBuilding
                && tradeInTerritory
                && !hex.getBuilding().isRuined()
                && (hex.getBuilding() instanceof TradingPost
                || viewModel.getTownHall().getLevel().getLevelN() >= 2)
                && !tradeAlreadyUsed;
        tradeButton.setEnabled(tradeAvailable);
        tradeButton.setToolTipText(
                !tradeBuilding
                        ? "Select a Bazaar or Trading Post."
                        : !tradeInTerritory
                        ? "The trade structure must be inside your territory."
                        : hex.getBuilding().isRuined()
                        ? "This trade structure is ruined."
                        : viewModel.getTownHall().getLevel().getLevelN() < 2
                        && hex.getBuilding() instanceof Bazaar
                        ? "A Bazaar requires Town Hall level 2."
                        : tradeAlreadyUsed
                        ? "This trade source has already been used this turn."
                        : "Open the trade window"
        );
        ruinButton.setVisible(hasHex && hex.getBuilding() != null);
        ruinButton.setEnabled(hasHex && hex.getBuilding() != null);
        wallSection.setVisible(!walls.isEmpty());
        ruinWallButton.setVisible(!walls.isEmpty());
        ruinWallButton.setEnabled(
                selectedWall != null
                        && selectedUnit instanceof Builder
                        && selectedUnit.getPosition() != null
                        && (selectedUnit.getPosition().equals(
                                selectedWall.getCoordinate1()
                        ) || selectedUnit.getPosition().equals(
                                selectedWall.getCoordinate2()
                        ))
        );
        stationButton.setEnabled(
                hasHex
                        && hex.getBuilding() instanceof ProductionBuilding
                        && viewState.getSelectedUnit() instanceof Worker
        );
        generateMilitaryUnitButton.setEnabled(
                hasHex && hex.getBuilding() instanceof MilitaryStable
        );
        expandButton.setEnabled(
                viewState.getSelectedUnit() instanceof BorderExpander
        );
        combatButton.setEnabled(
                hasHex
                        && viewModel.isDiscovered(viewState.getSelectedHex())
                        && hex.getUnits().stream().anyMatch(
                                unit -> unit instanceof MilitaryUnit
                                        && unit.isOwnedByPlayer()
                        )
        );
        directAttackButton.setEnabled(
                selectedUnit instanceof MilitaryUnit militaryUnit
                        && militaryUnit.isOwnedByPlayer()
                        && militaryUnit.getCurrentAP()
                        >= militaryUnit.getMilitaryType().getAttackAP()
        );
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

        text.append("Hex ")
                .append(
                        ViewTextFormatter.formatCoordinate(
                                selectedHex
                        )
                )
                .append("  •  ")
                .append(
                        viewModel.ownsTerritory(selectedHex)
                                ? "Your territory"
                                : "Outside territory"
                )
                .append('\n');

        if (discovered) {
            text.append("Terrain: ")
                    .append(ViewTextFormatter.pretty(hex.getTerrain()))
                    .append("  •  Units: ")
                    .append(hex.getUnits().size())
                    .append('\n');
            text.append("Building: ")
                    .append(ViewTextFormatter.formatBuilding(hex.getBuilding()))
                    .append('\n');
            text.append("Resources: ")
                    .append(
                            ViewTextFormatter.formatResources(
                                    hex.getAvailableResources()
                            )
                    )
                    .append('\n')
                    .append("Walls: ")
                    .append(ViewTextFormatter.formatWalls(
                            viewModel.getWalls(selectedHex)
                    ));
        } else {
            text.append("Undiscovered terrain  •  Resources hidden");
        }

        appendSelectedUnitInformation(text);
        return text.toString();
    }

    private void appendSelectedUnitInformation(
            StringBuilder text
    ) {
        Unit selectedUnit = viewState.getSelectedUnit();

        if (selectedUnit == null) {
            return;
        }

        text.append('\n')
                .append("Selected: ")
                .append(ViewTextFormatter.pretty(selectedUnit.getType()))
                .append("  ")
                .append(selectedUnit.getCurrentAP())
                .append('/')
                .append(selectedUnit.getType().getEachTurnAP());

        if (selectedUnit instanceof Builder builder) {
            text.append("  Charges: ")
                    .append(builder.getCharges());
        }

        if (selectedUnit instanceof Worker worker) {
            text.append("  Stationed: ")
                    .append(worker.isInBuilding() ? "Yes" : "No");
        }
    }

    JButton getBuildMenuButton() {
        return buildMenuButton;
    }

    JButton getBuildWallButton() {
        return buildWallButton;
    }

    JButton getTradeButton() {
        return tradeButton;
    }

    JButton getConfirmBuildButton() {
        return confirmBuildButton;
    }

    JButton getRuinButton() {
        return ruinButton;
    }

    JButton getRuinWallButton() {
        return ruinWallButton;
    }

    JButton getStationButton() {
        return stationButton;
    }

    JButton getGenerateMilitaryUnitButton() {
        return generateMilitaryUnitButton;
    }

    JButton getExpandButton() {
        return expandButton;
    }

    JButton getCombatButton() {
        return combatButton;
    }

    JButton getDirectAttackButton() {
        return directAttackButton;
    }

    BuildingType getSelectedBuildingType() {
        return (BuildingType) buildingCombo.getSelectedItem();
    }

    Wall getSelectedWall() {
        return (Wall) selectedWallCombo.getSelectedItem();
    }
}
