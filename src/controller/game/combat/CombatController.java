package controller.game.combat;

import model.game.combat.CombatStatus;
import model.game.combat.H2HCombat;
import model.game.hex.Hex;
import model.game.unit.military.MilitaryType;
import model.game.unit.military.MilitaryUnit;
import view.game.combat.CombatFrame;
import view.game.combat.CombatPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Coordinates the H2H combat model with the passive combat view. */
public final class CombatController {

    private static final int ROLL_TICK_MS = 60;
    private static final int ROLL_TICKS = 10;
    private static final int PAUSE_MS = 500;
    private static final int COMPARE_STEP_MS = 550;
    private static final int AFTER_COMPARE_MS = 700;

    private final H2HCombat combat;
    private final CombatPanel view;
    private final CombatFrame frame;
    private final Runnable onFinished;
    private final Random random = new Random();

    private boolean finished;

    private CombatController(H2HCombat combat, Runnable onFinished) {
        this.combat = combat;
        this.onFinished = onFinished == null ? () -> {} : onFinished;
        this.view = new CombatPanel(
                unitLines(combat.getAttackMilitaryUnits()),
                unitLines(combat.getDefenceMilitaryUnits()),
                this::handleAction
        );
        this.frame = new CombatFrame(view);
    }

    public static void launch(
            Hex attackHex,
            Hex defenceHex,
            boolean targetTribe,
            Runnable onFinished
    ) {
        SwingUtilities.invokeLater(() -> {
            H2HCombat combat = new H2HCombat(
                    attackHex,
                    defenceHex,
                    targetTribe
            );
            CombatStatus status = combat.startCombat();
            if (status != CombatStatus.STARTED_SUCCESSFULLY) {
                JOptionPane.showMessageDialog(
                        null,
                        "Combat cannot start: " + status.getMessage(),
                        "Combat Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            CombatController controller = new CombatController(
                    combat,
                    onFinished
            );
            controller.show();
        });
    }

    private void show() {
        frame.setVisible(true);
    }

    private void handleAction() {
        if (finished) {
            close();
            return;
        }
        startRound();
    }

    private void startRound() {
        view.setActionEnabled(false);
        view.appendLog("── New round ──");

        combat.throwDices();
        view.prepareDice(
                combat.getAttackDices().size(),
                combat.getDefenceDices().size()
        );
        startRolling();
    }

    private void startRolling() {
        final int[] tick = {0};
        Timer rollTimer = new Timer(ROLL_TICK_MS, null);
        rollTimer.addActionListener(event -> {
            tick[0]++;
            if (tick[0] < ROLL_TICKS) {
                view.showDiceValues(
                        randomValues(combat.getAttackDices().size()),
                        randomValues(combat.getDefenceDices().size())
                );
                return;
            }

            rollTimer.stop();
            view.showDiceValues(
                    combat.getAttackDices(),
                    combat.getDefenceDices()
            );
            view.appendLog(
                    "Rolled  A: " + combat.getAttackDices()
                            + "   D: " + combat.getDefenceDices()
            );
            startPauseThenSort();
        });
        rollTimer.start();
    }

    private void startPauseThenSort() {
        Timer pause = new Timer(PAUSE_MS, event -> {
            combat.sort();
            view.showDiceValues(
                    combat.getAttackDices(),
                    combat.getDefenceDices()
            );
            view.appendLog(
                    "Sorted  A: " + combat.getAttackDices()
                            + "   D: " + combat.getDefenceDices()
            );
            startCompareAnimation();
        });
        pause.setRepeats(false);
        pause.start();
    }

    private void startCompareAnimation() {
        List<Integer> attackDice = new ArrayList<>(combat.getAttackDices());
        List<Integer> defenceDice = new ArrayList<>(combat.getDefenceDices());
        int pairs = Math.min(attackDice.size(), defenceDice.size());
        final int[] index = {0};

        Timer step = new Timer(COMPARE_STEP_MS, null);
        step.addActionListener(event -> {
            if (index[0] >= pairs) {
                step.stop();
                startPauseThenResolve();
                return;
            }

            int pairIndex = index[0];
            int attackValue = attackDice.get(pairIndex);
            int defenceValue = defenceDice.get(pairIndex);
            if (attackValue > defenceValue) {
                view.highlightPair(
                        pairIndex,
                        CombatPanel.PairResult.ATTACKER_WINS
                );
                view.appendLog(
                        String.format(
                                "  pair %d:  A(%d) > D(%d)  → defender loses",
                                pairIndex + 1,
                                attackValue,
                                defenceValue
                        )
                );
            } else if (attackValue < defenceValue) {
                view.highlightPair(
                        pairIndex,
                        CombatPanel.PairResult.DEFENDER_WINS
                );
                view.appendLog(
                        String.format(
                                "  pair %d:  A(%d) < D(%d)  → attacker loses",
                                pairIndex + 1,
                                attackValue,
                                defenceValue
                        )
                );
            } else {
                view.highlightPair(pairIndex, CombatPanel.PairResult.TIE);
                view.appendLog(
                        String.format(
                                "  pair %d:  A(%d) = D(%d)  → tie",
                                pairIndex + 1,
                                attackValue,
                                defenceValue
                        )
                );
            }
            index[0]++;
        });
        step.start();
    }

    private void startPauseThenResolve() {
        Timer pause = new Timer(AFTER_COMPARE_MS, event -> {
            Map<MilitaryType, Integer> attackersBefore = countByType(
                    combat.getAttackMilitaryUnits()
            );
            Map<MilitaryType, Integer> defendersBefore = countByType(
                    combat.getDefenceMilitaryUnits()
            );

            combat.compare();
            combat.impact();

            logLosses(
                    "Attacker",
                    attackersBefore,
                    countByType(combat.getAttackMilitaryUnits())
            );
            logLosses(
                    "Defender",
                    defendersBefore,
                    countByType(combat.getDefenceMilitaryUnits())
            );

            view.setUnitColumns(
                    unitLines(combat.getAttackMilitaryUnits()),
                    unitLines(combat.getDefenceMilitaryUnits())
            );
            if (combat.isDone()) {
                finishCombat();
            } else {
                view.setActionEnabled(true);
            }
        });
        pause.setRepeats(false);
        pause.start();
    }

    private void finishCombat() {
        finished = true;
        view.appendLog("── Combat finished ──");
        view.appendLog(
                combat.getDefenceMilitaryUnits().isEmpty()
                        ? "Attacker wins!"
                        : "Defender wins!"
        );
        view.showDoneAction();
    }

    private void close() {
        view.closeScreen();
        onFinished.run();
    }

    private List<Integer> randomValues(int count) {
        List<Integer> values = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            values.add(random.nextInt(6) + 1);
        }
        return values;
    }

    private void logLosses(
            String side,
            Map<MilitaryType, Integer> before,
            Map<MilitaryType, Integer> after
    ) {
        boolean any = false;
        StringBuilder message = new StringBuilder(side + " losses: ");
        for (MilitaryType type : before.keySet()) {
            int lost = before.get(type) - after.getOrDefault(type, 0);
            if (lost > 0) {
                message.append('-')
                        .append(lost)
                        .append(' ')
                        .append(type.getName())
                        .append("  ");
                any = true;
            }
        }
        view.appendLog(any ? message.toString() : side + " losses: none");
    }

    private Map<MilitaryType, Integer> countByType(List<MilitaryUnit> units) {
        Map<MilitaryType, Integer> counts = new EnumMap<>(MilitaryType.class);
        for (MilitaryUnit unit : units) {
            counts.merge(unit.getMilitaryType(), 1, Integer::sum);
        }
        return counts;
    }

    private static List<String> unitLines(List<MilitaryUnit> units) {
        Map<MilitaryType, Integer> counts = new EnumMap<>(MilitaryType.class);
        for (MilitaryUnit unit : units) {
            counts.merge(unit.getMilitaryType(), 1, Integer::sum);
        }

        List<String> lines = new ArrayList<>();
        for (MilitaryType type : MilitaryType.values()) {
            Integer count = counts.get(type);
            if (count != null) {
                lines.add(count + "x " + type.getName());
            }
        }
        return lines;
    }
}
