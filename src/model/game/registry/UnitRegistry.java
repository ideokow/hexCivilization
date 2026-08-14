package model.game.registry;

import model.game.happiness.Era;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryType;
import model.game.unit.military.MilitaryUnit;

import java.util.HashMap;
import java.util.Map;

public class UnitRegistry {

    private static UnitRegistry instance;
    public static UnitRegistry getInstance() {
        if (instance == null) instance = new UnitRegistry();
        return instance;
    }

    private final Map<String, Unit> unitMap;

    public UnitRegistry() {
        unitMap = new HashMap<>();
    }

    public Map<String, Unit> getUnitMap() {
        return new HashMap<>(unitMap);
    }

    public void addUnit(Unit unit) {
        unitMap.put(unit.getUnitID(), unit);
        if (UnitType.isMilitary(unit.getType())) {
            MilitaryRegistry.getInstance().addUnit((MilitaryUnit) unit);
        }
    }

    public void renewUnitAPs(Era era) {
        for (Unit unit : unitMap.values()) {
            unit.resetAP(era);
        }
    }

    public void removeUnit(Unit unit) {
        unitMap.remove(unit.getUnitID());
        if (UnitType.isMilitary(unit.getType())) {
            MilitaryRegistry.getInstance().removeUnit((MilitaryUnit) unit);
        }
    }
}
