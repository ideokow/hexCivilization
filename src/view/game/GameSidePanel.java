package view.game;

import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.townhall.operation.AcquireTechnologyOperation;
import model.game.townhall.operation.GenerateUnitOperation;
import model.game.townhall.operation.LevelUpOperation;
import model.game.townhall.operation.TownHallOperation;
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
    private static final Color CARD_BORDER = new Color(58, 74, 96);

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
    private final JPanel routeSection;
    private JPanel townHallQueueCard;
    private JPanel acquiredTechnologiesCard;

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
        this.levelUpButton = new JButton("Upgrade");
        this.acquireTechnologyButton = new JButton("Research");
        this.generateUnitButton = new JButton("Generate");
        this.routeButton = new JButton("Set Route");
        this.clearRouteButton = new JButton("Clear Route");
        this.endTurnButton = new JButton("End Turn");
        this.resetCameraButton = new JButton("Reset Camera");
        this.routeSection = new JPanel();

        configurePanel();
        buildContent();
    }

    private void configurePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(312, 100));
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
        configureTownHallQueueArea();
        add(createTitle("Command Center"));
        add(Box.createVerticalStrut(10));
        townHallQueueCard = createInfoCard(
                "Town Hall Queue",
                townHallQueueArea
        );
        add(townHallQueueCard);
        add(Box.createVerticalStrut(10));

        configureAcquiredTechnologiesArea();
        acquiredTechnologiesCard = createInfoCard(
                "Acquired Technologies",
                acquiredTechnologiesArea
        );
        add(acquiredTechnologiesCard);
        add(Box.createVerticalStrut(12));

        configureLevelCombo();
        configureTechnologyCombo();
        configureUnitTypeCombo();
        add(createSectionLabel("Town Hall Operations"));
        add(Box.createVerticalStrut(6));
        add(createActionRow(levelCombo, levelUpButton));
        add(Box.createVerticalStrut(6));
        add(createActionRow(technologyCombo, acquireTechnologyButton));
        add(Box.createVerticalStrut(6));
        add(createActionRow(unitTypeCombo, generateUnitButton));

        add(Box.createVerticalGlue());
        addEndTurnButton();
        add(Box.createVerticalStrut(8));
        styleActionButton(resetCameraButton, 32);
        add(resetCameraButton);
        add(Box.createVerticalStrut(10));
        configureRouteSection();
        add(routeSection);

        clearRouteButton.setVisible(false);
        routeSection.setVisible(false);
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
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

    private JPanel createInfoCard(
            String title,
            JComponent content
    ) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
        );
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = createSectionLabel(title);
        titleLabel.setForeground(ACCENT);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        resizeInfoCard(card);
        return card;
    }

    private void resizeInfoCard(JPanel card) {
        card.setPreferredSize(null);
        Dimension preferredSize = card.getPreferredSize();
        int height = preferredSize.height;
        card.setPreferredSize(new Dimension(280, height));
        card.setMinimumSize(new Dimension(0, height));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private JPanel createActionRow(
            JComboBox<?> comboBox,
            JButton button
    ) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        styleActionButton(button, 32);
        button.setPreferredSize(new Dimension(92, 32));
        row.add(comboBox, BorderLayout.CENTER);
        row.add(button, BorderLayout.EAST);
        return row;
    }

    private void configureRouteSection() {
        routeSection.setLayout(new BoxLayout(routeSection, BoxLayout.Y_AXIS));
        routeSection.setBackground(CARD_BACKGROUND);
        routeSection.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
        );
        routeSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        routeSection.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 88)
        );

        JLabel title = createSectionLabel("Unit Route");
        title.setForeground(ACCENT);
        routeSection.add(title);
        routeSection.add(Box.createVerticalStrut(6));

        styleActionButton(routeButton, 30);
        routeSection.add(routeButton);
        routeSection.add(Box.createVerticalStrut(5));

        styleActionButton(clearRouteButton, 30);
        routeSection.add(clearRouteButton);
    }

    private void configureTownHallQueueArea() {
        townHallQueueArea.setEditable(false);
        townHallQueueArea.setOpaque(true);
        townHallQueueArea.setBackground(CARD_BACKGROUND);
        townHallQueueArea.setForeground(TEXT_PRIMARY);
        townHallQueueArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        townHallQueueArea.setBorder(BorderFactory.createEmptyBorder());
        townHallQueueArea.setLineWrap(true);
        townHallQueueArea.setWrapStyleWord(true);
        townHallQueueArea.setColumns(20);
        townHallQueueArea.setRows(1);
    }

    private void configureAcquiredTechnologiesArea() {
        acquiredTechnologiesArea.setEditable(false);
        acquiredTechnologiesArea.setOpaque(true);
        acquiredTechnologiesArea.setBackground(CARD_BACKGROUND);
        acquiredTechnologiesArea.setForeground(TEXT_PRIMARY);
        acquiredTechnologiesArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        acquiredTechnologiesArea.setBorder(BorderFactory.createEmptyBorder());
        acquiredTechnologiesArea.setLineWrap(true);
        acquiredTechnologiesArea.setWrapStyleWord(true);
        acquiredTechnologiesArea.setColumns(20);
        acquiredTechnologiesArea.setRows(1);
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
                new Dimension(Integer.MAX_VALUE, 30)
        );
        comboBox.setPreferredSize(
                new Dimension(160, 30)
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

    private void styleActionButton(JButton button, int height) {
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
    }

    private void addEndTurnButton() {
        endTurnButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        endTurnButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 52)
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
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)
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
            townHallQueueArea.setRows(1);
        } else {
            townHallQueueArea.setText(buildOperationText(operation));
            townHallQueueArea.setRows(2);
        }
        resizeInfoCard(townHallQueueCard);

        levelUpButton.setEnabled(queueAvailable);
        acquireTechnologyButton.setEnabled(queueAvailable);
        generateUnitButton.setEnabled(queueAvailable);
        revalidate();
        repaint();
    }

    private void refreshTechnologyDisplay() {
        List<Technology> acquired =
                viewModel.getAcquiredTechnologies();

        if (acquired.isEmpty()) {
            acquiredTechnologiesArea.setText("None");
            acquiredTechnologiesArea.setRows(1);
        } else {
            StringBuilder text = new StringBuilder();
            for (Technology technology : acquired) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(ViewTextFormatter.pretty(technology));
            }
            acquiredTechnologiesArea.setText(text.toString());
            acquiredTechnologiesArea.setRows(acquired.size());
        }
        resizeInfoCard(acquiredTechnologiesCard);

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
        routeSection.setVisible(unitSelected);
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
