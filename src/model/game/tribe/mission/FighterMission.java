package model.game.tribe.mission;

import model.game.registry.MilitaryRegistry;
import model.game.registry.UnitRegistry;
import model.game.tribe.Tribe;
import model.game.unit.military.Swordsman;

public class FighterMission extends Mission {

    public FighterMission(Tribe tribe) {
        super(tribe, MissionType.MILITARY_ASSISTANCE);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(20);
        for (int i = 0; i<3; i++) {
            if (MilitaryRegistry.getInstance().getMilitaryUnitsNumber() == getTribe().getRelatedTownHall().getMilitaryUnitCap()) {
                break; // intentionally i don't decrease happiness
            }
            Swordsman newOne = new Swordsman(getTribe().getRelatedTownHall().getOwner(), getTribe().getLocation());
            newOne.resetAP(getTribe().getRelatedTownHall().getHappiness().getEra());
            UnitRegistry.getInstance().addUnit(newOne);
        }
    }

    @Override
    public boolean checkRequirements() {
        return false;
        // TODO : check requirement
        // if its ok don't forget return true and setIsReady()
    }
}
