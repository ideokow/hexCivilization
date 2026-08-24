package model.game.building;

import controller.game.GameEngine;
import controller.game.UnitMap;
import model.game.hex.HexCoordinate;
import model.game.townhall.TownHall;
import model.game.townhall.opration.GenerationStatus;
import model.game.unit.*;
import model.game.unit.military.Cavalry;


public class MilitaryStable extends Building {

    public MilitaryStable(boolean ownedByPlayer, HexCoordinate position) {
        super(BuildingType.MILITARY_STABLE, ownedByPlayer, position);
    }

    public GenerationStatus canGenerateUnit(TownHall townHall, UnitMap unitMap) {
        if (townHall.getUnitNumber() >= townHall.getUnitCap())
            return GenerationStatus.UNIT_CAP_REACHED;
        if (!townHall.canAfford(UnitType.CAVALRY.getCost()))
            return GenerationStatus.CANT_AFFORD_COST;
        if (townHall.getLevel().getLevelN() < UnitType.CAVALRY.getMinimumLevel().getLevelN())
            return GenerationStatus.NOT_ENOUGH_LEVEL;
        if (unitMap.getMilitaryUnitsNumber() >= townHall.getMilitaryUnitCap()) {
            return GenerationStatus.MILITARY_UNIT_CAP_REACHED;
        }
        return GenerationStatus.SUCCESS;
    }

    public void generateUnit(TownHall townHall, UnitMap unitMap) {
        if (!canGenerateUnit(townHall, unitMap).equals(GenerationStatus.SUCCESS)) return;

        // make unit
        Cavalry unit = new Cavalry(getPosition());

        // full ap
        unit.resetAP(townHall.getHappiness().getEra());

        // add to registry
        unitMap.addUnit(unit);

        // military cap reach impact on public contest
        if (unitMap.getMilitaryUnitsNumber() == townHall.getMilitaryUnitCap()) {
            townHall.getHappiness().addHappiness(-1);
        }

        // place unit on hex
        townHall.getGrid().get(new HexCoordinate(0, 0)).addUnit(unit);
        townHall.increaseUnitNumber();
    }
}
