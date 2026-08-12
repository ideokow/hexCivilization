package model.game.tribe.mission;

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class FighterMission extends Mission {

    public FighterMission(Tribe tribe) {
        super(tribe, MissionType.MILITARY_ASSISTANCE, MissionType.MILITARY_ASSISTANCE.getDeadLine());
    }

    @Override
    public void payReward(TownHall townHall) {

    }

    @Override
    public boolean checkRequirements() {
        return false;
    }
}
