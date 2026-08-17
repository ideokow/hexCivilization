package model.game.combat;

import model.game.hex.Hex;
import model.game.hex.HexGrid;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryType;
import model.game.unit.military.MilitaryUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class H2HCombat {

    private final Random generator = new Random();

    // parameters

    private final Hex  attackHex;
    private final Hex defenceHex;

    private boolean isNeighborHex;
    private final boolean fightWithTribe; // target is tribe or animal

    // game-state

    private List<Integer>  attackDices;
    private List<Integer> defenceDices;
    private List<Boolean>     compares;

    private boolean inProgress = false;

    public H2HCombat(Hex attackHex, Hex defenceHex, boolean isTargetTribe) {
        this.attackHex = attackHex;
        this.defenceHex = defenceHex;
        fightWithTribe = isTargetTribe;
    }

    /* start combat process */
    public CombatStatus startCombat() {
        if (inProgress) return CombatStatus.ALREADY_IN_COMBAT;

        // check distance
        int dist = HexGrid.calculateDistance(attackHex.getCoordinate(), defenceHex.getCoordinate());
        if (dist > 2) return CombatStatus.FAR_HEXES;
        if (dist == 0) return CombatStatus.SAME_HEXES;
        isNeighborHex = dist == 1;

        // check units
        List<MilitaryUnit> mUnits = getAttackMilitaryUnits();
        CombatStatus checkUnitsStatus = checkUnits(mUnits);
        if (!checkUnitsStatus.equals(CombatStatus.STARTED_SUCCESSFULLY)) return checkUnitsStatus;

        // check target
        List<MilitaryUnit> dUnits = getDefenceMilitaryUnits();
        if (dUnits.size() == 0) return CombatStatus.NO_DEFENDER;

        // ap check
        for (MilitaryUnit unit : mUnits) {
            if (unit.getCurrentAP() == 0) return CombatStatus.NOT_ENOUGH_AP;
        }

        // start war
        inProgress = true;

        // decrease ap
        for (MilitaryUnit unit : mUnits) {
            unit.spendAP(1);
        }

        return CombatStatus.STARTED_SUCCESSFULLY;
    }

    /*
    Main Progress functions
      1. throw dices
      2. sort dices
      3. compare them
      4. impact
    */
    public void throwDices() {
        if (!inProgress) return;

        int attackerTypes = 0;
        attackerTypes += containsType(getAttackMilitaryUnits(), MilitaryType.SWORDSMAN) ? 1 : 0;
        attackerTypes += containsType(getAttackMilitaryUnits(), MilitaryType.ARCHER)    ? 1 : 0;
        attackerTypes += containsType(getAttackMilitaryUnits(), MilitaryType.CAVALRY)   ? 1 : 0;

        attackDices  = throwNDices(attackerTypes);
        defenceDices = throwNDices(fightWithTribe ? 2 : 1);
    }

    public void sort() {
        if (!inProgress) return;
        attackDices .sort(Comparator.reverseOrder());
        defenceDices.sort(Comparator.reverseOrder());
    }

    public void compare() {
        if (!inProgress) return;
        List<Boolean> diceCompare = new ArrayList<>();
        for (int i=0; i<Math.min(attackDices.size(), defenceDices.size()); i++) {
            diceCompare.add(attackDices.get(i) > defenceDices.get(i));
        }
        compares = diceCompare;
    }

    public void impact() {
        if (!inProgress || compares == null) return;

        int attackerHits = 0;
        int defenderHits = 0;

        for (boolean c : compares) {
            if (c) attackerHits++;
            else defenderHits++;
        }

        int attackersAttack = getAttackValue(getAttackMilitaryUnits());
        int defendersAttack = getAttackValue(getDefenceMilitaryUnits());

        applyHits(getAttackMilitaryUnits(), getDefenceMilitaryUnits(), defenderHits, defendersAttack);
        applyHits(getDefenceMilitaryUnits(), getAttackMilitaryUnits(), attackerHits, attackersAttack);

        inProgress = false;
    }

    private void applyHits(List<MilitaryUnit> units, List<MilitaryUnit> attackers, int hits, int attack) {
        for (int i = 0; i < hits; i++) {
            MilitaryUnit target = pickTarget(units);
            if (target == null) return; // done!
            target.increaseHp(-attack);
        }
    }

    private int getAttackValue(List<MilitaryUnit> attackers) {
        int sum = attackers
                .stream()
                .mapToInt(attacker -> attacker.getMilitaryType().getAttackValue())
                .sum();
        if (!attackers.isEmpty()) return sum / attackers.size();
        else return 0;
    }

    /* check unit number condition check */
    private CombatStatus checkUnits(List<MilitaryUnit> mUnits) {
        int s = 0, a = 0, c = 0;
        for (MilitaryUnit mUnit : mUnits) {
            if      (mUnit.getMilitaryType().equals(MilitaryType.SWORDSMAN)) s++;
            else if (mUnit.getMilitaryType().equals(MilitaryType.ARCHER   )) a++;
            else if (mUnit.getMilitaryType().equals(MilitaryType.CAVALRY  )) c++;
        }
        if (s > 2 || a > 2 || c > 1) return CombatStatus.TOO_MANY_UNITS;
        if (s == 0 && a == 0 && c == 0) return CombatStatus.NO_UNITS;
        return CombatStatus.STARTED_SUCCESSFULLY;
    }

    // --- Utils ---

    private List<Integer> throwNDices(int N) {
        List<Integer> numbers = new ArrayList<>();
        for (int i=0; i < N; i++) numbers.add(throwOneDice());
        return numbers;
    }

    private int throwOneDice() {
        return generator.nextInt(6) + 1;
    }

    private List<MilitaryUnit> getAttackMilitaryUnits() {
        List<Unit> units = attackHex.getUnits();
        List<MilitaryUnit> mUnits = new ArrayList<>();
        for (Unit unit : units) {
            if (unit instanceof MilitaryUnit &&
                    (isNeighborHex || unit.getType().equals(UnitType.ARCHER))) {
                mUnits.add((MilitaryUnit) unit);
            }
        }
        return mUnits;
    }

    private List<MilitaryUnit> getDefenceMilitaryUnits() {
        List<Unit> units = defenceHex.getUnits();
        List<MilitaryUnit> mUnits = new ArrayList<>();
        for (Unit unit : units) {
            if (unit instanceof MilitaryUnit) {
                mUnits.add((MilitaryUnit) unit);
            }
        }
        return mUnits;
    }

    private MilitaryUnit pickTarget(List<MilitaryUnit> units) {
        MilitaryType[] priority = {
                MilitaryType.SWORDSMAN,
                MilitaryType.ARCHER,
                MilitaryType.CAVALRY
        };

        for (MilitaryType type : priority) {
            for (MilitaryUnit unit : units) {
                if (unit.getHp() > 0 && unit.getMilitaryType().equals(type)) {
                    return unit;
                }
            }
        }
        return null;
    }

    private boolean containsType(List<MilitaryUnit> militaryUnits, MilitaryType type) {
        for (MilitaryUnit unit : militaryUnits) {
            if (unit.getMilitaryType().equals(type)) return true;
        }
        return false;
    }

    // --- Getters ---

    public Hex getAttackHex() {
        return attackHex;
    }

    public Hex getDefenceHex() {
        return defenceHex;
    }

    public List<Integer> getAttackDices() {
        return attackDices;
    }

    public List<Integer> getDefenceDices() {
        return defenceDices;
    }

    public List<Boolean> getCompares() {
        return compares;
    }
}
