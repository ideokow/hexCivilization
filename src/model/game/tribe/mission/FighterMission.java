package model.game.tribe.mission;

import model.game.tribe.Tribe;

public class FighterMission extends Mission {

    public FighterMission(Tribe tribe) {
        super(tribe, MissionType.MILITARY_ASSISTANCE);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(20);
        // TODO : give townHall 3 Swordsman
    }

    @Override
    public boolean checkRequirements() {
        return false;
        // TODO : check requirement
    }
}
