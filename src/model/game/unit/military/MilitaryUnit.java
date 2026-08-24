package model.game.unit.military;

import controller.game.BuildingMap;
import controller.game.UnitMap;
import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Wall;
import model.game.tribe.Tribe;
import model.game.unit.Unit;

public abstract class MilitaryUnit extends Unit {
    public MilitaryUnit(HexCoordinate position, MilitaryType militaryType) {
        super(position);
        this.militaryType = militaryType;
    }

    public MilitaryUnit(HexCoordinate position, MilitaryType militaryType, Tribe tribe) {
        super(position, tribe);
        this.militaryType = militaryType;
    }

    private final MilitaryType militaryType;

    public void attack(UnitMap unitMap, Unit unit) {
        if (unit == null || unit.equals(this) || !canAttack()) return;

        unitMap.changeHp(unit, -getMilitaryType().getAttackValue());
        spendAP(getMilitaryType().getAttackAP());
    }

    public void attack(BuildingMap buildingMap, Building building) {
        if (building == null || !canAttack()) return;

        buildingMap.changeHp(building, -getMilitaryType().getAttackValue());
        spendAP(getMilitaryType().getAttackAP());
    }

    public void attack(HexGrid hexGrid, Wall wall) {
        if (wall == null || !canAttack()) return;

        hexGrid.getWallLayer().addHp(wall, -getMilitaryType().getAttackValue());
        spendAP(getMilitaryType().getAttackAP());
    }

    private boolean canAttack() {
        return getCurrentAP() >= getMilitaryType().getAttackAP();
    }

    public MilitaryType getMilitaryType() {
        return militaryType;
    }
}
