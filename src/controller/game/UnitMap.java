package controller.game;

import model.game.happiness.Era;
import model.game.hex.HexGrid;
import model.game.registry.MilitaryRegistry;
import model.game.townhall.TownHall;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryUnit;

import java.util.HashMap;
import java.util.Map;

public class UnitMap {

    private final Map<String, Unit> map;

    public UnitMap() {
        map = new HashMap<>();
    }

    public Map<String, Unit> getMap() {
        return new HashMap<>(map);
    }

    public void addUnit(Unit unit) {
        map.put(unit.getUnitID(), unit);
        if (UnitType.isMilitary(unit.getType())) {
            MilitaryRegistry.getInstance().addUnit((MilitaryUnit) unit);
        }
    }

    public void renewUnitAPs(Era era) {
        for (Unit unit : map.values()) {
            unit.resetAP(era);
        }
    }

    public void removeUnit(Unit unit) {
        map.remove(unit.getUnitID());
        if (UnitType.isMilitary(unit.getType())) {
            MilitaryRegistry.getInstance().removeUnit((MilitaryUnit) unit);
        }
    }

    public void killUnit(HexGrid hexGrid, TownHall townHall, Unit unit) {
        removeUnit(unit);
        hexGrid.get(unit.getPosition()).removeUnit(unit);
        townHall.decreaseUnitNumber();
    }
}
