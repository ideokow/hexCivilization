package model.game.tribe.mission;

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class MountaineerMission extends Mission {

    public MountaineerMission(Tribe tribe) {
        super(tribe, MissionType.MINING_TOOLS, MissionType.MINING_TOOLS.getDeadLine());
    }

    @Override
    public void payReward(TownHall townHall) {

    }

    @Override
    public boolean checkRequirements() {
        return false;
    }
}
