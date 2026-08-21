package view.game;

import model.game.hex.Resource;
import model.game.townhall.TownHall;
import model.game.trade.TradeLevel;
import model.game.trade.TribeTradeStrategy;
import model.game.tribe.Tribe;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Small modal editor for the three Phase 1 trade variants.
 *
 * The dialog only collects the player's choices. The controller remains
 * responsible for executing the trade through the model.
 */
public final class TradeDialog {

    public enum Source {
        BAZAAR,
        TRADING_POST,
        TRIBE
    }

    public record TradeRequest(
            Resource soldResource,
            Resource receivedResource,
            TradeLevel level,
            int amount
    ) {
    }

    private static final Color BACKGROUND = new Color(27, 35, 49);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color TEXT_PRIMARY = new Color(234, 238, 245);
    private static final Color TEXT_MUTED = new Color(157, 171, 191);
    private static final Color ACCENT = new Color(238, 190, 78);
    private static final Color ACTION_BLUE = new Color(52, 91, 132);
    private static final Color BORDER = new Color(88, 112, 140);

    private TradeDialog() {
    }

    public static TradeRequest open(
            Component parent,
            Source source,
            TownHall townHall,
            Tribe tribe
    ) {
        if (source == null || townHall == null) {
            return null;
        }

        List<Resource> receivedResources = source == Source.TRIBE && tribe != null
                ? Arrays.stream(Resource.values())
                .filter(tribe.getTradeResources()::contains)
                .toList()
                : List.of(Resource.values());
        if (receivedResources.isEmpty()) {
            return null;
        }

        JDialog dialog = new JDialog(
                windowOf(parent),
                dialogTitle(source, tribe),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JComboBox<Resource> soldCombo = new JComboBox<>(Resource.values());
        JComboBox<Resource> receivedCombo =
                new JComboBox<>(receivedResources.toArray(Resource[]::new));
        styleCombo(soldCombo);
        styleCombo(receivedCombo);

        JComboBox<TradeLevel> levelCombo = null;
        JSpinner amountSpinner = null;
        if (source == Source.BAZAAR) {
            levelCombo = new JComboBox<>(TradeLevel.values());
            styleCombo(levelCombo);
        } else {
            amountSpinner = new JSpinner(
                    new SpinnerNumberModel(
                            1,
                            1,
                            Math.max(1, townHall.getResourceCap()),
                            1
                    )
            );
            styleSpinner(amountSpinner);
        }
        final JComboBox<TradeLevel> selectedLevelCombo = levelCombo;
        final JSpinner selectedAmountSpinner = amountSpinner;

        JLabel availableLabel = new JLabel();
        JLabel previewLabel = new JLabel();
        JLabel errorLabel = new JLabel(" ");
        configureLabel(availableLabel, TEXT_MUTED, Font.PLAIN, 11);
        configureLabel(previewLabel, TEXT_PRIMARY, Font.BOLD, 12);
        configureLabel(errorLabel, new Color(238, 133, 125), Font.PLAIN, 11);

        JButton cancelButton = new JButton("Cancel");
        JButton tradeButton = new JButton("Trade");
        styleButton(cancelButton, new Color(66, 76, 91));
        styleButton(tradeButton, ACTION_BLUE);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 0);

        JLabel heading = new JLabel("Choose the resources to exchange");
        configureLabel(heading, ACCENT, Font.BOLD, 14);
        content.add(heading, constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.weightx = 0;
        addLabel(content, "Sell", constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        content.add(soldCombo, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 0;
        addLabel(content, "Receive", constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        content.add(receivedCombo, constraints);

        if (source == Source.BAZAAR) {
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.weightx = 0;
            addLabel(content, "Trade level", constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            content.add(levelCombo, constraints);
        } else {
            constraints.gridx = 0;
            constraints.gridy++;
            constraints.weightx = 0;
            addLabel(content, "Amount sold", constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            content.add(amountSpinner, constraints);
        }

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(8, 0, 0, 0);
        content.add(availableLabel, constraints);

        constraints.gridy++;
        content.add(previewLabel, constraints);

        constraints.gridy++;
        content.add(errorLabel, constraints);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(tradeButton);
        constraints.gridy++;
        constraints.insets = new Insets(8, 0, 0, 0);
        content.add(buttons, constraints);

        Runnable refresh = () -> {
            int soldAmount = source == Source.BAZAAR
                    ? ((TradeLevel) selectedLevelCombo.getSelectedItem()).getSoldAmount()
                    : (Integer) selectedAmountSpinner.getValue();
            Resource soldResource = (Resource) soldCombo.getSelectedItem();
            Resource receivedResource = (Resource) receivedCombo.getSelectedItem();
            int available = townHall.getResourceAmount(soldResource);
            int rate = conversionRate(source, tribe, selectedLevelCombo);
            int receivedAmount = (int) (((long) soldAmount * rate) / 100L);

            availableLabel.setText(
                    "Available " + soldResource.getDisplayName() + ": " + available
                            + "  •  Free "
                            + receivedResource.getDisplayName()
                            + " storage: "
                            + townHall.getAvailableStorage(receivedResource)
            );
            previewLabel.setText(
                    "Preview: -" + soldAmount + " "
                            + soldResource.getDisplayName()
                            + "  →  +" + receivedAmount + " "
                            + receivedResource.getDisplayName()
                            + "  (" + rate + "%)"
            );

            if (source != Source.BAZAAR) {
                updateAmountMaximum(selectedAmountSpinner, available);
                soldAmount = (Integer) selectedAmountSpinner.getValue();
                receivedAmount = (int) (((long) soldAmount * rate) / 100L);
                previewLabel.setText(
                        "Preview: -" + soldAmount + " "
                                + soldResource.getDisplayName()
                                + "  →  +" + receivedAmount + " "
                                + receivedResource.getDisplayName()
                                + "  (" + rate + "%)"
                );
            }

            boolean valid = soldResource != null
                    && receivedResource != null
                    && soldResource != receivedResource
                    && soldAmount > 0
                    && available >= soldAmount
                    && townHall.getAvailableStorage(receivedResource) >= receivedAmount;
            tradeButton.setEnabled(valid);
            if (soldResource == receivedResource) {
                errorLabel.setText("Choose two different resources.");
            } else if (available < soldAmount) {
                errorLabel.setText("You do not have enough of the sold resource.");
            } else if (townHall.getAvailableStorage(receivedResource) < receivedAmount) {
                errorLabel.setText("The Town Hall has no room for the received resource.");
            } else {
                errorLabel.setText(" ");
            }
        };

        soldCombo.addActionListener(event -> refresh.run());
        receivedCombo.addActionListener(event -> refresh.run());
        if (selectedLevelCombo != null) {
            selectedLevelCombo.addActionListener(event -> refresh.run());
        }
        if (selectedAmountSpinner != null) {
            selectedAmountSpinner.addChangeListener(event -> refresh.run());
        }

        final TradeRequest[] result = new TradeRequest[1];
        cancelButton.addActionListener(event -> dialog.dispose());
        tradeButton.addActionListener(event -> {
            Resource soldResource = (Resource) soldCombo.getSelectedItem();
            Resource receivedResource = (Resource) receivedCombo.getSelectedItem();
            TradeLevel level = selectedLevelCombo == null
                    ? null
                    : (TradeLevel) selectedLevelCombo.getSelectedItem();
            int amount = source == Source.BAZAAR
                    ? level.getSoldAmount()
                    : (Integer) selectedAmountSpinner.getValue();

            if (soldResource == receivedResource
                    || townHall.getResourceAmount(soldResource) < amount) {
                refresh.run();
                return;
            }

            result[0] = new TradeRequest(
                    soldResource,
                    receivedResource,
                    level,
                    amount
            );
            dialog.dispose();
        });

        dialog.setContentPane(content);
        refresh.run();
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static int conversionRate(
            Source source,
            Tribe tribe,
            JComboBox<TradeLevel> levelCombo
    ) {
        if (source == Source.BAZAAR) {
            TradeLevel level = (TradeLevel) levelCombo.getSelectedItem();
            return level.getConversionRatePercent();
        }
        if (source == Source.TRIBE && tribe != null) {
            return new TribeTradeStrategy(tribe).getConversionRatePercent();
        }
        return 80;
    }

    private static String dialogTitle(Source source, Tribe tribe) {
        return switch (source) {
            case BAZAAR -> "Bazaar Trade";
            case TRADING_POST -> "Trading Post Trade";
            case TRIBE -> tribe == null
                    ? "Tribe Trade"
                    : ViewTextFormatter.pretty(tribe.getTribeType()) + " Tribe Trade";
        };
    }

    private static void updateAmountMaximum(JSpinner spinner, int available) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        int maximum = Math.max(1, available);
        model.setMaximum(maximum);
        if ((Integer) model.getValue() > maximum) {
            model.setValue(maximum);
        }
    }

    private static void addLabel(
            JPanel panel,
            String text,
            GridBagConstraints constraints
    ) {
        JLabel label = new JLabel(text);
        configureLabel(label, TEXT_MUTED, Font.BOLD, 11);
        constraints.insets = new Insets(0, 0, 7, 10);
        panel.add(label, constraints);
        constraints.insets = new Insets(0, 0, 7, 0);
    }

    private static void configureLabel(
            JLabel label,
            Color color,
            int style,
            int size
    ) {
        label.setForeground(color);
        label.setFont(new Font("SansSerif", style, size));
    }

    private static void styleCombo(JComboBox<?> comboBox) {
        comboBox.setBackground(CONTROL_BACKGROUND);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        selected,
                        false
                );
                if (value instanceof Resource resource) {
                    label.setText(resource.getDisplayName());
                } else if (value instanceof Enum<?> enumValue) {
                    label.setText(ViewTextFormatter.pretty(enumValue));
                }
                label.setForeground(TEXT_PRIMARY);
                label.setBackground(selected
                        ? new Color(62, 83, 108)
                        : CONTROL_BACKGROUND);
                return label;
            }
        });
    }

    private static void styleSpinner(JSpinner spinner) {
        spinner.setBackground(CONTROL_BACKGROUND);
        spinner.setForeground(TEXT_PRIMARY);
        spinner.setBorder(BorderFactory.createLineBorder(BORDER));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setBackground(CONTROL_BACKGROUND);
            defaultEditor.getTextField().setForeground(TEXT_PRIMARY);
            defaultEditor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private static void styleButton(JButton button, Color background) {
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(background);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
        );
    }

    private static Window windowOf(Component component) {
        return component instanceof Window
                ? (Window) component
                : SwingUtilities.getWindowAncestor(component);
    }
}
