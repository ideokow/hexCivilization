package model.game.unit.military;

public enum MilitaryType {
    SWORDSMAN("Swordsman", 2,15, 1),
    ARCHER   ("Archer"   , 2,25, 2),
    CAVALRY  ("Cavalry"  , 4,40, 1);

    private final String name;
    private final int AttackAP;
    private final int AttackValue;
    private final int AttackRange;

    MilitaryType(String name, int attackAP, int attackValue, int attackRange) {
        this.name = name;
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

    public String getName() {
        return name;
    }
}
