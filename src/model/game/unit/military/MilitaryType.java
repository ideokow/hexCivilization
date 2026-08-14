package model.game.unit.military;

public enum MilitaryType {
    SWORDSMAN(2,15, 1),
    ARCHER   (2,25, 2),
    CAVALRY  (4,40, 1);

    private final int AttackAP;
    private final int AttackValue;
    private final int AttackRange;

    MilitaryType(int attackAP, int attackValue, int attackRange) {
        AttackAP = attackAP;
        AttackValue = attackValue;
        AttackRange = attackRange;
    }

    public int getAttackAP() {
        return AttackAP;
    }

    public int getAttackValue() {
        return AttackValue;
    }

    public int getAttackRange() {
        return AttackRange;
    }
}
