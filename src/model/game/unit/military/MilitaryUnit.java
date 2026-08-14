package model.game.unit.military;

import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.unit.Unit;

public abstract class MilitaryUnit extends Unit {
    public MilitaryUnit(Player owner, HexCoordinate position, MilitaryType militaryType) {
        super(owner, position);
        this.militaryType = militaryType;
    }

    private final MilitaryType militaryType;

    public void attack(Unit unit) {
        if (getCurrentAP() >= getMilitaryType().getAttackAP()) {
            unit.increaseHp(getMilitaryType().getAttackAP());
            spendAP(getMilitaryType().getAttackAP());
        }
    }

    public void attack(Building building) {
        if (getCurrentAP() >= getMilitaryType().getAttackAP()) {
            building.increaseHp(getMilitaryType().getAttackAP());
            spendAP(getMilitaryType().getAttackAP());
        }
    }

    // TODO : implement wall attack

    public MilitaryType getMilitaryType() {
        return militaryType;
    }
}
