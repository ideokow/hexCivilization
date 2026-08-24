package model.game.tribe.mission;

import controller.game.GameEngine;
import model.game.building.BuildingType;
import model.game.building.TribeCamp;
import model.game.hex.Hex;
import model.game.tribe.Tribe;
import model.game.unit.military.Swordsman;

import java.util.List;

public class FighterMission extends Mission {

    public FighterMission(Tribe tribe) {
        super(tribe, MissionType.MILITARY_ASSISTANCE);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(20);
        for (int i = 0; i<3; i++) {
            if (GameEngine.getInstance().getUnitMap().getMilitaryUnitsNumber() == getTribe().getRelatedTownHall().getMilitaryUnitCap()) {
                break; // intentionally don't decrease happiness
            }
            Swordsman newOne = new Swordsman(getTribe().getLocation());
            newOne.resetAP(getTribe().getRelatedTownHall().getHappiness().getEra());
            GameEngine.getInstance().getUnitMap().addUnit(newOne);
        }
    }

    @Override
    public boolean checkRequirements() {
        List<Hex> hexesInRange = getTribe().getRelatedTownHall().getGrid()
                .hexesInRange(getTribe().getLocation(), 5);
        for (Hex hex : hexesInRange) {
            if (hex.getBuilding() != null
                    && hex.getBuilding().getType().equals(BuildingType.TRIBE_CAMP) &&
                    ((TribeCamp) hex.getBuilding()).getTribe().getLastLostTurn() > getAcquireTurn()) {
                setIsReady();
                return true;
            }
        }

        return false;
    }
}
