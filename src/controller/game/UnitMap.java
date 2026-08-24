package controller.game;

import model.game.happiness.Era;
import model.game.hex.HexGrid;
import model.game.townhall.TownHall;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitMap {

    private final Map<String, Unit> map;
    private final List<String> militaryIDs;

    public UnitMap() {
        map = new HashMap<>();
        militaryIDs = new ArrayList<>();
    }

    public Map<String, Unit> getMap() {
        return new HashMap<>(map);
    }

    public void addUnit(Unit unit) {
        map.put(unit.getUnitID(), unit);
        if (UnitType.isMilitary(unit.getType())) {
            militaryIDs.add(unit.getUnitID());
        }
    }

    public void renewUnitAPs(Era era) {
        for (Unit unit : map.values()) {
            unit.resetAP(era);
        }
    }

    public void removeUnit(Unit unit) {
        map.remove(unit.getUnitID());

        // remove if unit is military
        militaryIDs.remove(unit.getUnitID());
    }

    public void killUnit(HexGrid hexGrid, TownHall townHall, Unit unit) {
        removeUnit(unit);
        hexGrid.get(unit.getPosition()).removeUnit(unit);
        townHall.decreaseUnitNumber();
    }

    public int getMilitaryUnitsNumber() {
        return militaryIDs.size();
    }
}
