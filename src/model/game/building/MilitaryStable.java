package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.registry.MilitaryRegistry;
import model.game.registry.UnitRegistry;
import model.game.townhall.TownHall;
import model.game.townhall.opration.GenerationStatus;
import model.game.unit.*;
import model.game.unit.military.Cavalry;


public class MilitaryStable extends Building {

    public MilitaryStable(boolean ownedByPlayer, HexCoordinate position) {
        super(BuildingType.MILITARY_STABLE, ownedByPlayer, position);
    }

    public GenerationStatus canGenerateUnit(TownHall townHall) {
        if (townHall.getUnitNumber() >= townHall.getUnitCap())
            return GenerationStatus.UNIT_CAP_REACHED;
        if (!townHall.canAfford(UnitType.CAVALRY.getCost()))
            return GenerationStatus.CANT_AFFORD_COST;
        if (townHall.getLevel().getLevelN() < UnitType.CAVALRY.getMinimumLevel().getLevelN())
            return GenerationStatus.NOT_ENOUGH_LEVEL;
        if (MilitaryRegistry.getInstance().getMilitaryUnitsNumber() >= townHall.getMilitaryUnitCap()) {
            return GenerationStatus.MILITARY_UNIT_CAP_REACHED;
        }
        return GenerationStatus.SUCCESS;
    }

    public void generateUnit(TownHall townHall) {
        if (!canGenerateUnit(townHall).equals(GenerationStatus.SUCCESS)) return;

        // make unit
        Cavalry unit = new Cavalry(isOwnedByPlayer(), getPosition());

        // full ap
        unit.resetAP(townHall.getHappiness().getEra());

        // add to registry
        UnitRegistry.getInstance().addUnit(unit);

        // military cap reach impact on public contest
        if (MilitaryRegistry.getInstance().getMilitaryUnitsNumber() == townHall.getMilitaryUnitCap()) {
            townHall.getHappiness().addHappiness(-1);
        }

        // place unit on hex
        townHall.getGrid().get(new HexCoordinate(0, 0)).addUnit(unit);
        townHall.increaseUnitNumber();
    }
}
