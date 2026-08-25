package controller.game.map;

import model.game.happiness.Era;
import model.game.hex.HexGrid;
import model.game.townhall.TownHall;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitMap implements Serializable {

    private final HexGrid hexGrid;
    private TownHall townHall;

    private final Map<String, Unit> map;
    private final List<String> militaryIDs;

    public UnitMap(HexGrid hexGrid) {
        this.hexGrid = hexGrid;
        map = new HashMap<>();
        militaryIDs = new ArrayList<>();
    }

    public void setTownHall(TownHall townHall) {
        this.townHall = townHall;
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

    public int getMilitaryUnitsNumber() {
        return militaryIDs.size();
    }

    // damage and die

    public void changeHp(Unit unit, int amount) {
        unit.setHp(Math.max(0, Math.min(unit.getHpCap(), unit.getHp() + amount)));
        if (unit.getHp() == 0) killUnit(unit);
    }

    public void killUnit(Unit unit) {
        map.remove(unit.getUnitID());
        hexGrid.get(unit.getPosition()).removeUnit(unit);
        townHall.decreaseUnitNumber();

        // remove if unit is military
        militaryIDs.remove(unit.getUnitID());
    }
}
