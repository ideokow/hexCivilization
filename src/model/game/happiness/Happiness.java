package model.game.happiness;

import controller.game.BuildingMap;
import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.hex.HexGrid;
import model.game.townhall.TownHall;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import java.util.Collection;
import java.util.List;

public class Happiness {

    private int value;

    public Happiness() {
        value = 0;
    }

    public void addHappiness(int amount) {
        value += amount;
    }

    public void checkMonuments(BuildingMap buildingMap) {
        Collection<Building> allBuildings = buildingMap.getMap().values();
        for (Building building: allBuildings) {
            if (building.getType().equals(BuildingType.MONUMENT)) {
                addHappiness(2);
            }
        }
    }

    public void checkTownHallMilitary(TownHall townHall, HexGrid grid) {
        List<Unit> units = grid.get(townHall.getPosition()).getUnits();
        for (Unit unit : units) {
            if (UnitType.isMilitary(unit.getType())) {
                addHappiness(1);
                return;
            }
        }
    }

    public Era getEra() {
        if (value >= 3) {
            return Era.GOLDEN_ERA;
        }
        else if (value >= -2) {
            return Era.NORMAL_ERA;
        }
        else if (value >= -4) {
            return Era.DISCONTENT_ERA;
        }
        else {
            return Era.REBELLION_ERA;
        }
    }

    public int getValue() {
        return value;
    }
}
