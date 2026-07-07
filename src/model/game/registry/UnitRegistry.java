package model.game.registry;

import model.game.unit.Unit;

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
    }

    public void renewUnitAPs() {
        for (Unit unit : unitMap.values()) {
            unit.resetAP();
        }
    }
}
