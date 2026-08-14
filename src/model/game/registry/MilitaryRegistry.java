package model.game.registry;

import model.game.unit.military.MilitaryUnit;

import java.util.HashMap;
import java.util.Map;

public class MilitaryRegistry {

    private static MilitaryRegistry instance;
    public static MilitaryRegistry getInstance() {
        if (instance == null) instance = new MilitaryRegistry();
        return instance;
    }

    private final Map<String, MilitaryUnit> unitMap;

    public MilitaryRegistry() {
        unitMap = new HashMap<>();
    }

    public void addUnit(MilitaryUnit unit) {
        unitMap.put(unit.getUnitID(), unit);
    }

    public Map<String, MilitaryUnit> getUnits() {
        return new HashMap<>(unitMap);
    }

    public void removeUnit(MilitaryUnit unit) {
        unitMap.remove(unit.getUnitID());
    }

    public int getMilitaryUnitsNumber() {
        return unitMap.size();
    }
}
