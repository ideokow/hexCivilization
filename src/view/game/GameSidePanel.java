package view.game;

import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.townhall.opration.AcquireTechnologyOperation;
import model.game.townhall.opration.GenerateUnitOperation;
import model.game.townhall.opration.LevelUpOperation;
import model.game.townhall.opration.TownHallOperation;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

final class GameSidePanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(17, 23, 33);
    private static final Color CARD_BACKGROUND = new Color(27, 35, 49);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color TEXT_PRIMARY = new Color(234, 238, 245);
    private static final Color TEXT_MUTED = new Color(157, 171, 191);
    private static final Color ACCENT = new Color(238, 190, 78);
    private static final Color ACTION_BLUE = new Color(52, 91, 132);
    private static final Color ACTION_GREEN = new Color(47, 143, 105);

    private final GameViewModel viewModel;
    private final JTextArea townHallQueueArea;
    private final JTextArea acquiredTechnologiesArea;
    private final DefaultComboBoxModel<Technology> technologyModel;
    private final JComboBox<Level> levelCombo;
    private final JComboBox<Technology> technologyCombo;
    private final JComboBox<UnitType> unitTypeCombo;
    private final JButton levelUpButton;
    private final JButton acquireTechnologyButton;
    private final JButton generateUnitButton;
    private final JButton routeButton;
    private final JButton clearRouteButton;
    private final JButton endTurnButton;
    private final JButton resetCameraButton;

    GameSidePanel(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.townHallQueueArea = new JTextArea();
        this.acquiredTechnologiesArea = new JTextArea();
        this.technologyModel = new DefaultComboBoxModel<>();
        this.levelCombo = new JComboBox<>(
                new Level[]{Level.LEVEL_2, Level.LEVEL_3}
        );
        this.technologyCombo = new JComboBox<>(technologyModel);
        this.unitTypeCombo = new JComboBox<>(UnitType.values());
        this.levelUpButton = new JButton("Upgrade Town Hall");
        this.acquireTechnologyButton = new JButton("Research Technology");
        this.generateUnitButton = new JButton("Generate Unit");
        this.routeButton = new JButton("Set Route");
        this.clearRouteButton = new JButton("Clear Route");
        this.endTurnButton = new JButton("End Turn");
        this.resetCameraButton = new JButton("Reset Camera");

        configurePanel();
        buildContent();
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
        add(createTitle("Town Hall Queue"));
        add(Box.createVerticalStrut(8));
        configureTownHallQueueArea();
        add(townHallQueueArea);
        add(Box.createVerticalStrut(12));

        add(createSectionLabel("Acquired technologies"));
        add(Box.createVerticalStrut(4));
        configureAcquiredTechnologiesArea();
        add(acquiredTechnologiesArea);
        add(Box.createVerticalStrut(10));

        configureLevelCombo();
        add(levelCombo);
        add(Box.createVerticalStrut(6));
        addActionButton(levelUpButton, 34);
        add(Box.createVerticalStrut(8));

        configureTechnologyCombo();
        add(technologyCombo);
        add(Box.createVerticalStrut(6));
        addActionButton(acquireTechnologyButton, 34);
        add(Box.createVerticalStrut(8));

        configureUnitTypeCombo();
        add(unitTypeCombo);
        add(Box.createVerticalStrut(6));
        addActionButton(generateUnitButton, 34);
        add(Box.createVerticalStrut(18));

        add(createTitle("Unit Routes"));
        add(Box.createVerticalStrut(8));
        addActionButton(routeButton, 34);
        add(Box.createVerticalStrut(6));
        addActionButton(clearRouteButton, 34);
        add(Box.createVerticalStrut(18));

        addEndTurnButton();
        add(Box.createVerticalStrut(8));
        addActionButton(resetCameraButton, 34);

        clearRouteButton.setVisible(false);
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(ACCENT);
        label.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0,
                                0,
                                1,
                                0,
                                new Color(67, 78, 95)
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

    private void configureAcquiredTechnologiesArea() {
        acquiredTechnologiesArea.setEditable(false);
        acquiredTechnologiesArea.setOpaque(true);
        acquiredTechnologiesArea.setBackground(CARD_BACKGROUND);
        acquiredTechnologiesArea.setForeground(TEXT_PRIMARY);
        acquiredTechnologiesArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        acquiredTechnologiesArea.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(47, 61, 80)),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)
                )
        );
        acquiredTechnologiesArea.setLineWrap(true);
        acquiredTechnologiesArea.setWrapStyleWord(true);
        acquiredTechnologiesArea.setRows(2);
        acquiredTechnologiesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureLevelCombo() {
        styleComboBox(levelCombo);
    }

    private void configureTechnologyCombo() {
        styleComboBox(technologyCombo);
    }

    private void configureUnitTypeCombo() {
        styleComboBox(unitTypeCombo);
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
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
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

                if (value instanceof Enum<?> enumValue) {
                    label.setText(ViewTextFormatter.pretty(enumValue));
                }

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

    private void addActionButton(JButton button, int height) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, height)
        );
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(ACTION_BLUE);
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

    private void addEndTurnButton() {
        endTurnButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        endTurnButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 56)
        );
        endTurnButton.setForeground(Color.WHITE);
        endTurnButton.setBackground(ACTION_GREEN);
        endTurnButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        endTurnButton.setFocusPainted(false);
        endTurnButton.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(119, 204, 163),
                                1
                        ),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
        );
        endTurnButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(endTurnButton);
    }

    void refreshTownHallQueue() {
        refreshTechnologyDisplay();

        TownHallOperation operation = viewModel.getInQueueOperation();
        boolean queueAvailable = operation == null;

        if (operation == null) {
            townHallQueueArea.setText("Empty");
        } else {
            townHallQueueArea.setText(buildOperationText(operation));
        }

        levelUpButton.setEnabled(queueAvailable);
        acquireTechnologyButton.setEnabled(queueAvailable);
        generateUnitButton.setEnabled(queueAvailable);
    }

    private void refreshTechnologyDisplay() {
        List<Technology> acquired =
                viewModel.getAcquiredTechnologies();

        if (acquired.isEmpty()) {
            acquiredTechnologiesArea.setText("None");
        } else {
            StringBuilder text = new StringBuilder();
            for (Technology technology : acquired) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(ViewTextFormatter.pretty(technology));
            }
            acquiredTechnologiesArea.setText(text.toString());
        }

        Technology selected =
                (Technology) technologyCombo.getSelectedItem();
        boolean selectedStillAvailable =
                selected != null && !acquired.contains(selected);

        technologyModel.removeAllElements();
        for (Technology technology : Technology.values()) {
            if (!acquired.contains(technology)) {
                technologyModel.addElement(technology);
            }
        }

        if (selectedStillAvailable) {
            technologyCombo.setSelectedItem(selected);
        } else if (technologyModel.getSize() > 0) {
            technologyCombo.setSelectedIndex(0);
        }

        technologyCombo.setEnabled(technologyModel.getSize() > 0);
    }

    private String buildOperationText(TownHallOperation operation) {
        String operationName;

        if (operation instanceof GenerateUnitOperation generate) {
            operationName = "Generating "
                    + ViewTextFormatter.pretty(generate.getUnitType());
        } else if (operation instanceof AcquireTechnologyOperation acquire) {
            operationName = "Researching "
                    + ViewTextFormatter.pretty(acquire.getTechnology());
        } else if (operation instanceof LevelUpOperation levelUp) {
            operationName = "Upgrading to Town Hall level "
                    + levelUp.getLevel().getLevelN();
        } else {
            operationName = ViewTextFormatter.pretty(operation.getType());
        }

        return operationName
                + "\nRemaining turns: "
                + operation.getTurnsRemaining();
    }

    void setRouteControls(boolean unitSelected, boolean routeExists) {
        routeButton.setEnabled(unitSelected);
        routeButton.setVisible(unitSelected);
        clearRouteButton.setVisible(unitSelected && routeExists);
        revalidate();
        repaint();
    }

    JButton getLevelUpButton() {
        return levelUpButton;
    }

    JButton getAcquireTechnologyButton() {
        return acquireTechnologyButton;
    }

    JButton getGenerateUnitButton() {
        return generateUnitButton;
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

    Level getSelectedLevel() {
        return (Level) levelCombo.getSelectedItem();
    }

    Technology getSelectedTechnology() {
        return (Technology) technologyCombo.getSelectedItem();
    }

    UnitType getSelectedUnitType() {
        return (UnitType) unitTypeCombo.getSelectedItem();
    }
}
