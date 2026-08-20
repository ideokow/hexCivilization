package view.game.combat;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Passive combat view. Combat rules and phase transitions are handled by the
 * controller; this panel only renders state and reports button presses.
 */
public class CombatPanel extends JPanel {

    public enum PairResult {
        ATTACKER_WINS,
        DEFENDER_WINS,
        TIE
    }

    private static final Color WIN_COLOR = new Color(198, 239, 206);
    private static final Color LOSE_COLOR = new Color(255, 199, 206);
    private static final Color TIE_COLOR = new Color(255, 235, 156);

    private final JPanel attackDiceColumn = new JPanel();
    private final JPanel defenceDiceColumn = new JPanel();
    private final JButton actionButton = new JButton("Throw");
    private final List<DieView> attackDieViews = new ArrayList<>();
    private final List<DieView> defenceDieViews = new ArrayList<>();
    private final JPanel westHolder = new JPanel(new BorderLayout());
    private final JPanel eastHolder = new JPanel(new BorderLayout());
    private final JTextArea logArea = new JTextArea();
    private final Runnable actionHandler;

    public CombatPanel(
            List<String> attackerLines,
            List<String> defenderLines,
            Runnable actionHandler
    ) {
        this.actionHandler = actionHandler == null ? () -> {} : actionHandler;
        buildUi(attackerLines, defenderLines);
        actionButton.addActionListener(event -> this.actionHandler.run());
    }

    private void buildUi(
            List<String> attackerLines,
            List<String> defenderLines
    ) {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        setUnitColumns(attackerLines, defenderLines);
        add(westHolder, BorderLayout.WEST);
        add(buildDicesArea(), BorderLayout.CENTER);
        add(eastHolder, BorderLayout.EAST);
        add(buildLogArea(), BorderLayout.SOUTH);
    }

    public void setUnitColumns(
            List<String> attackerLines,
            List<String> defenderLines
    ) {
        westHolder.removeAll();
        eastHolder.removeAll();
        westHolder.add(
                buildUnitsColumn("Attackers:", attackerLines),
                BorderLayout.CENTER
        );
        eastHolder.add(
                buildUnitsColumn("Defenders:", defenderLines),
                BorderLayout.CENTER
        );
        westHolder.revalidate();
        eastHolder.revalidate();
        westHolder.repaint();
        eastHolder.repaint();
    }

    private JComponent buildUnitsColumn(String title, List<String> lines) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        panel.setPreferredSize(new Dimension(180, 0));

        JLabel header = new JLabel(title);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(16));

        for (String line : lines) {
            JLabel label = new JLabel(line);
            label.setFont(label.getFont().deriveFont(15f));
            label.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(10));
        }
        return panel;
    }

    private JComponent buildDicesArea() {
        JPanel center = new JPanel(new BorderLayout(0, 8));

        JLabel title = new JLabel("dices", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        center.add(title, BorderLayout.NORTH);

        JPanel dice = new JPanel(new GridLayout(1, 2, 12, 0));
        dice.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        attackDiceColumn.setLayout(new BoxLayout(attackDiceColumn, BoxLayout.Y_AXIS));
        defenceDiceColumn.setLayout(new BoxLayout(defenceDiceColumn, BoxLayout.Y_AXIS));
        dice.add(wrapScroll(attackDiceColumn));
        dice.add(wrapScroll(defenceDiceColumn));
        center.add(dice, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionButton.setFont(actionButton.getFont().deriveFont(Font.BOLD, 14f));
        south.add(actionButton);
        center.add(south, BorderLayout.SOUTH);

        center.setPreferredSize(new Dimension(320, 0));
        return center;
    }

    private JComponent buildLogArea() {
        logArea.setEditable(false);
        logArea.setRows(6);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Combat log"));
        scrollPane.setPreferredSize(new Dimension(0, 170));
        return scrollPane;
    }

    private JScrollPane wrapScroll(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }

    public void prepareDice(int attackCount, int defenceCount) {
        prepareDieViews(attackDiceColumn, attackDieViews, attackCount);
        prepareDieViews(defenceDiceColumn, defenceDieViews, defenceCount);
    }

    public void showDiceValues(
            List<Integer> attackValues,
            List<Integer> defenceValues
    ) {
        showValues(attackDieViews, attackValues);
        showValues(defenceDieViews, defenceValues);
    }

    public void highlightPair(int index, PairResult result) {
        Color attackerColor;
        Color defenderColor;
        switch (result) {
            case ATTACKER_WINS -> {
                attackerColor = WIN_COLOR;
                defenderColor = LOSE_COLOR;
            }
            case DEFENDER_WINS -> {
                attackerColor = LOSE_COLOR;
                defenderColor = WIN_COLOR;
            }
            case TIE -> {
                attackerColor = TIE_COLOR;
                defenderColor = TIE_COLOR;
            }
            default -> throw new IllegalStateException("Unknown pair result");
        }
        highlight(attackDieViews, index, attackerColor);
        highlight(defenceDieViews, index, defenderColor);
    }

    public void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public void setActionEnabled(boolean enabled) {
        actionButton.setEnabled(enabled);
    }

    public void showDoneAction() {
        actionButton.setText("Done");
        actionButton.setEnabled(true);
    }

    public void closeScreen() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private void prepareDieViews(
            JPanel column,
            List<DieView> views,
            int count
    ) {
        column.removeAll();
        views.clear();
        for (int index = 0; index < count; index++) {
            DieView view = new DieView();
            view.setAlignmentX(CENTER_ALIGNMENT);
            views.add(view);
            column.add(Box.createVerticalStrut(8));
            column.add(view);
        }
        column.revalidate();
        column.repaint();
    }

    private void showValues(List<DieView> views, List<Integer> values) {
        if (values == null) {
            return;
        }
        int count = Math.min(views.size(), values.size());
        for (int index = 0; index < count; index++) {
            views.get(index).clearHighlight();
            views.get(index).setValue(values.get(index));
        }
    }

    private void highlight(List<DieView> views, int index, Color color) {
        if (index < views.size()) {
            views.get(index).setHighlight(color);
        }
    }
}
