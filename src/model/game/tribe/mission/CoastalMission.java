package model.game.tribe.mission;

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class CoastalMission extends Mission {

    public CoastalMission(Tribe tribe) {
        super(tribe, MissionType.COASTAL_DEVELOPMENT, MissionType.COASTAL_DEVELOPMENT.getDeadLine());
    }

    @Override
    public void payReward(TownHall townHall) {

    }

    @Override
    public boolean checkRequirements() {
        return false;
    }
}
