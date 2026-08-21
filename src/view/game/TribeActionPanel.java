package view.game;

import model.game.hex.Resource;
import model.game.tribe.Tribe;
import model.game.tribe.mission.Mission;
import model.game.tribe.mission.MissionState;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

final class TribeActionPanel extends JPanel {

    private static final Color PANEL_BACKGROUND = new Color(8, 12, 18, 222);
    private static final Color PANEL_BORDER = new Color(190, 207, 225, 210);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color TEXT_PRIMARY = new Color(240, 244, 250);
    private static final Color TEXT_MUTED = new Color(169, 183, 202);
    private static final Color ACCENT = new Color(238, 190, 78);
    private static final Color ACTION_BLUE = new Color(52, 91, 132);
    private static final Color ACTION_GREEN = new Color(47, 143, 105);
    private static final Color ACTION_RED = new Color(128, 61, 70);

    private static final Resource[] RESOURCE_ORDER = {
            Resource.FOOD,
            Resource.WOOD,
            Resource.STONE,
            Resource.IRON
    };

    private final GameViewModel viewModel;
    private final GameViewState viewState;
    private final JTextArea tribeInfoArea;
    private final EnumMap<Resource, JSpinner> giftSpinners;
    private final JButton giftButton;
    private final JButton tradeButton;
    private final JButton warDeclarationButton;
    private final JButton peaceRequestButton;
    private final JButton allianceRequestButton;
    private final JButton closeButton;
    private final JLabel peacePaymentLabel;
    private final JPanel missionSection;
    private final JLabel missionTypeLabel;
    private final JLabel missionStateLabel;
    private final JTextArea missionDescriptionArea;
    private final JButton acquireMissionButton;
    private final JButton cancelMissionButton;
    private final JButton deliverMissionButton;

    TribeActionPanel(
            GameViewModel viewModel,
            GameViewState viewState
    ) {
        this.viewModel = viewModel;
        this.viewState = viewState;
        this.tribeInfoArea = new JTextArea();
        this.giftSpinners = new EnumMap<>(Resource.class);
        this.giftButton = new JButton("Send Gift");
        this.tradeButton = new JButton("Start Trade");
        this.warDeclarationButton = new JButton("Declare War");
        this.peaceRequestButton = new JButton("Request Peace");
        this.allianceRequestButton = new JButton("Request Alliance");
        this.closeButton = new JButton("×");
        this.peacePaymentLabel = new JLabel();
        this.missionSection = new JPanel();
        this.missionTypeLabel = new JLabel();
        this.missionStateLabel = new JLabel();
        this.missionDescriptionArea = new JTextArea();
        this.acquireMissionButton = new JButton("Acquire");
        this.cancelMissionButton = new JButton("Cancel");
        this.deliverMissionButton = new JButton("Deliver");

        configurePanel();
        buildContent();
        configureListeners();
        setVisible(false);
    }

    private void configurePanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setMaximumSize(new Dimension(430, 700));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(
                Math.max(370, preferredSize.width),
                preferredSize.height
        );
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();
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

        JLabel title = new JLabel("Tribe Camp");
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

        configureTribeInfoArea();
        add(tribeInfoArea);

        add(createSectionLabel("Gift resources"));
        add(Box.createVerticalStrut(5));
        add(createGiftSelector());
        add(Box.createVerticalStrut(5));
        addActionButton(giftButton, ACTION_GREEN);
        add(Box.createVerticalStrut(7));
        addActionButton(tradeButton, ACTION_BLUE);
        add(Box.createVerticalStrut(7));

        peacePaymentLabel.setForeground(TEXT_MUTED);
        peacePaymentLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        peacePaymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(peacePaymentLabel);
        add(Box.createVerticalStrut(5));
        addActionButton(warDeclarationButton, ACTION_RED);
        add(Box.createVerticalStrut(5));
        addActionButton(peaceRequestButton, ACTION_BLUE);
        add(Box.createVerticalStrut(5));
        addActionButton(allianceRequestButton, ACTION_BLUE);
    }

    private void configureMissionSection() {
        missionSection.setOpaque(false);
        missionSection.setLayout(new BoxLayout(missionSection, BoxLayout.Y_AXIS));
        missionSection.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        missionSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = createSectionLabel("Missions");
        title.setForeground(ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        missionSection.add(title);
        missionSection.add(Box.createVerticalStrut(5));

        missionSection.add(createSectionLabel("Mission type"));
        missionTypeLabel.setForeground(TEXT_PRIMARY);
        missionTypeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        missionTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        missionSection.add(missionTypeLabel);
        missionSection.add(Box.createVerticalStrut(5));

        missionSection.add(createSectionLabel("Description"));
        configureMissionDescriptionArea();
        missionSection.add(missionDescriptionArea);
        missionSection.add(Box.createVerticalStrut(5));

        missionSection.add(createSectionLabel("Mission state"));
        missionStateLabel.setForeground(TEXT_PRIMARY);
        missionStateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        missionStateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        missionSection.add(missionStateLabel);
        missionSection.add(Box.createVerticalStrut(7));

        JPanel missionActions = new JPanel(new GridLayout(1, 3, 5, 0));
        missionActions.setOpaque(false);
        missionActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        missionActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        missionActions.add(styleActionButton(acquireMissionButton, ACTION_GREEN));
        missionActions.add(styleActionButton(cancelMissionButton, ACTION_RED));
        missionActions.add(styleActionButton(deliverMissionButton, ACTION_BLUE));
        missionSection.add(missionActions);
    }

    private void configureMissionDescriptionArea() {
        missionDescriptionArea.setEditable(false);
        missionDescriptionArea.setOpaque(false);
        missionDescriptionArea.setBackground(new Color(0, 0, 0, 0));
        missionDescriptionArea.setForeground(TEXT_PRIMARY);
        missionDescriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        missionDescriptionArea.setLineWrap(true);
        missionDescriptionArea.setWrapStyleWord(true);
        missionDescriptionArea.setRows(3);
        missionDescriptionArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        missionDescriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void configureTribeInfoArea() {
        tribeInfoArea.setEditable(false);
        tribeInfoArea.setOpaque(false);
        tribeInfoArea.setBackground(new Color(0, 0, 0, 0));
        tribeInfoArea.setForeground(TEXT_PRIMARY);
        tribeInfoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tribeInfoArea.setBorder(
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
        tribeInfoArea.setLineWrap(true);
        tribeInfoArea.setWrapStyleWord(true);
        tribeInfoArea.setRows(8);
        tribeInfoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        tribeInfoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JPanel createGiftSelector() {
        JPanel selector = new JPanel(new GridLayout(2, 4, 5, 5));
        selector.setOpaque(false);
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        selector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        for (Resource resource : RESOURCE_ORDER) {
            JLabel label = createSectionLabel(resource.getDisplayName());
            label.setHorizontalAlignment(SwingConstants.CENTER);
            selector.add(label);
        }

        for (Resource resource : RESOURCE_ORDER) {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
            spinner.setToolTipText("Amount of " + resource.getDisplayName() + " to gift");
            styleSpinner(spinner);
            giftSpinners.put(resource, spinner);
            selector.add(spinner);
        }

        return selector;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.PLAIN, 12));
        spinner.setForeground(TEXT_PRIMARY);
        spinner.setBackground(CONTROL_BACKGROUND);
        spinner.setFocusable(false);
        spinner.setPreferredSize(new Dimension(74, 28));
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setBackground(CONTROL_BACKGROUND);
            defaultEditor.getTextField().setForeground(TEXT_PRIMARY);
            defaultEditor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private void addActionButton(JButton button, Color background) {
        add(styleActionButton(button, background));
    }

    private JButton styleActionButton(JButton button, Color background) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(background);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(112, 137, 164)),
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)
                )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void configureListeners() {
        configureMissionSection();

        closeButton.addActionListener(event -> setVisible(false));
        for (JSpinner spinner : giftSpinners.values()) {
            spinner.addChangeListener(event -> refreshActionAvailability());
        }
    }

    void openForSelection() {
        refreshSelectionPanel();
        setVisible(true);
        revalidate();
        repaint();
    }

    void refreshSelectionPanel() {
        Tribe tribe = getSelectedTribe();
        if (tribe == null || !viewModel.isDiscovered(viewState.getSelectedHex())) {
            tribeInfoArea.setText("Select a discovered tribe camp to see its details.");
            peacePaymentLabel.setText("");
            removeMissionSection();
            refreshActionAvailability();
            return;
        }

        tribeInfoArea.setText(buildTribeText(tribe));
        refreshMissionSection(tribe);
        peacePaymentLabel.setText(
                tribe.isAtWar()
                        ? "Peace cost: 30 Food  •  30 Wood  •  30 Iron"
                        : ""
        );
        refreshActionAvailability();
    }

    private String buildTribeText(Tribe tribe) {
        StringBuilder text = new StringBuilder();
        text.append("Type: ")
                .append(ViewTextFormatter.pretty(tribe.getTribeType()))
                .append('\n')
                .append("Relation: ")
                .append(tribe.getRelation())
                .append('\n')
                .append("Camp: ")
                .append(ViewTextFormatter.formatCoordinate(tribe.getLocation()))
                .append('\n')
                .append("Mood: ")
                .append(ViewTextFormatter.pretty(tribe.getMood()))
                .append('\n')
                .append("Trade resources: ")
                .append(formatTradeResources(tribe));

        if (tribe.isSuspicious()) {
            text.append('\n')
                    .append("The tribe is suspicious of you.");
        }
        if (tribe.isAtWar()) {
            text.append('\n')
                    .append("You are at war with this tribe.");
        }
        if (tribe.isPeaceRequestActive()) {
            text.append('\n')
                    .append("Peace request sent.");
        }

        text.append('\n')
                .append(formatReward(tribe));
        return text.toString();
    }

    private void refreshMissionSection(Tribe tribe) {
        Mission mission = tribe.getCurrentMission();
        if (mission == null) {
            removeMissionSection();
            return;
        }

        if (missionSection.getParent() != this) {
            add(missionSection, 3);
        }

        if (mission.getMissionState() == MissionState.ACTIVE) {
            mission.checkRequirements();
        }

        missionTypeLabel.setText(ViewTextFormatter.pretty(mission.getMissionType()));
        missionDescriptionArea.setText(mission.getMissionType().getDescription());
        missionStateLabel.setText(ViewTextFormatter.pretty(mission.getMissionState()));
        refreshMissionActionAvailability(tribe, mission);
        revalidate();
        repaint();
    }

    private void removeMissionSection() {
        if (missionSection.getParent() == this) {
            remove(missionSection);
            revalidate();
            repaint();
        }

        acquireMissionButton.setEnabled(false);
        cancelMissionButton.setEnabled(false);
        deliverMissionButton.setEnabled(false);
    }

    private void refreshMissionActionAvailability(Tribe tribe, Mission mission) {
        boolean validSelection = viewModel.isDiscovered(viewState.getSelectedHex());
        MissionState missionState = mission.getMissionState();

        boolean canAcquire = validSelection
                && tribe.canAcquireMission()
                && missionState == MissionState.AVAILABLE;
        acquireMissionButton.setEnabled(canAcquire);
        acquireMissionButton.setToolTipText(
                canAcquire
                        ? "Accept this mission"
                        : "This mission cannot be acquired right now"
        );

        boolean canCancel = validSelection && missionState == MissionState.ACTIVE;
        cancelMissionButton.setEnabled(canCancel);
        cancelMissionButton.setToolTipText(
                canCancel
                        ? "Cancel the active mission"
                        : "Only active missions can be canceled"
        );

        boolean canDeliver = validSelection
                && missionState == MissionState.READY_TO_DELIVER;
        deliverMissionButton.setEnabled(canDeliver);
        deliverMissionButton.setToolTipText(
                canDeliver
                        ? "Deliver the completed mission"
                        : "Complete the mission requirements first"
        );
    }

    private String formatReward(Tribe tribe) {
        String reward = formatResources(tribe.getPotentialReward());
        return tribe.isAllianceActive()
                ? "Reward received: " + reward
                : "Potential reward: " + reward;
    }

    private String formatResources(Map<Resource, Integer> resources) {
        if (resources == null || resources.isEmpty()) {
            return "None";
        }

        StringBuilder text = new StringBuilder();
        for (Resource resource : RESOURCE_ORDER) {
            int amount = resources.getOrDefault(resource, 0);
            if (amount <= 0) {
                continue;
            }
            if (text.length() > 0) {
                text.append("  •  ");
            }
            text.append('+')
                    .append(amount)
                    .append(' ')
                    .append(resource.getDisplayName());
        }
        return text.length() == 0 ? "None" : text.toString();
    }

    private void refreshActionAvailability() {
        Tribe tribe = getSelectedTribe();
        boolean validSelection = tribe != null
                && viewModel.isDiscovered(viewState.getSelectedHex());

        Map<Resource, Integer> gift = getGiftResources();
        boolean canAffordGift = viewModel.getTownHall().canAfford(gift);
        giftButton.setEnabled(validSelection && !tribe.isEnemy() && !gift.isEmpty() && canAffordGift);
        giftButton.setToolTipText(
                canAffordGift
                        ? "Send the selected resources to this tribe"
                        : "The Town Hall cannot afford this gift"
        );

        boolean canTrade = validSelection
                && tribe.canTrade()
                && !tribe.getTradeResources().isEmpty()
                && !viewModel.hasTradedWithTribeThisTurn(tribe);
        tradeButton.setEnabled(canTrade);
        tradeButton.setToolTipText(
                canTrade
                        ? "Exchange resources with this tribe"
                        : tribe == null
                        ? "Select a discovered tribe camp."
                        : tribe.isEnemy()
                        ? "Enemy tribes do not trade."
                        : tribe.getRelation() < 20
                        ? "Trade requires a relation of at least 20."
                        : viewModel.hasTradedWithTribeThisTurn(tribe)
                        ? "This tribe has already been traded with this turn."
                        : "This tribe has no resources available for trade."
        );

        warDeclarationButton.setEnabled(validSelection && !tribe.isEnemy());

        Map<Resource, Integer> peacePayment = getPeaceRequestResources();
        boolean canAffordPeace = viewModel.getTownHall().canAfford(peacePayment);
        boolean canRequestPeace = validSelection
                && tribe.canRequestPeace(peacePayment, viewModel.getTurnNumber())
                && canAffordPeace;
        peaceRequestButton.setEnabled(canRequestPeace);
        peaceRequestButton.setToolTipText(
                canAffordPeace
                        ? "Pay the peace request and wait three turns"
                        : "Requires 30 Food, 30 Wood, and 30 Iron"
        );

        boolean canRequestAlliance = validSelection
                && tribe.canRequestAlliance(viewModel.getTurnNumber());
        allianceRequestButton.setEnabled(canRequestAlliance);
        allianceRequestButton.setToolTipText(
                canRequestAlliance
                        ? "Request an alliance"
                        : "Alliance requires a relation of at least 70"
        );
    }

    private Tribe getSelectedTribe() {
        return viewModel.getTribe(viewState.getSelectedHex());
    }

    private Map<Resource, Integer> getGiftResources() {
        EnumMap<Resource, Integer> resources = new EnumMap<>(Resource.class);
        for (Resource resource : RESOURCE_ORDER) {
            int amount = (Integer) giftSpinners.get(resource).getValue();
            if (amount > 0) {
                resources.put(resource, amount);
            }
        }
        return resources;
    }

    private String formatTradeResources(Tribe tribe) {
        if (tribe.getTradeResources().isEmpty()) {
            return "None";
        }
        return tribe.getTradeResources().stream()
                .map(Resource::getDisplayName)
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("None");
    }

    Map<Resource, Integer> getSelectedGiftResources() {
        return getGiftResources();
    }

    Map<Resource, Integer> getPeaceRequestResources() {
        EnumMap<Resource, Integer> resources = new EnumMap<>(Resource.class);
        resources.put(Resource.FOOD, 30);
        resources.put(Resource.WOOD, 30);
        resources.put(Resource.IRON, 30);
        return resources;
    }

    Tribe getSelectedTribeForAction() {
        return getSelectedTribe();
    }

    JButton getGiftButton() {
        return giftButton;
    }

    JButton getTradeButton() {
        return tradeButton;
    }

    JButton getWarDeclarationButton() {
        return warDeclarationButton;
    }

    JButton getPeaceRequestButton() {
        return peaceRequestButton;
    }

    JButton getAllianceRequestButton() {
        return allianceRequestButton;
    }

    JButton getAcquireMissionButton() {
        return acquireMissionButton;
    }

    JButton getCancelMissionButton() {
        return cancelMissionButton;
    }

    JButton getDeliverMissionButton() {
        return deliverMissionButton;
    }
}
