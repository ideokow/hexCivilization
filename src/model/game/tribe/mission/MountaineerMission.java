package model.game.tribe.mission;

import model.game.hex.Resource;
import model.game.tribe.Tribe;

public class MountaineerMission extends Mission {

    public MountaineerMission(Tribe tribe) {
        super(tribe, MissionType.MINING_TOOLS);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(15);
        getTribe().getRelatedTownHall().addStoneToStorage(20);
    }

    @Override
    public boolean checkRequirements() {
        if (
            !getMissionWarehouse().containsKey(Resource.WOOD) ||
            getMissionWarehouse().get(Resource.WOOD) < 15
        ) {
            return false;
        }
        if (
            !getMissionWarehouse().containsKey(Resource.IRON) ||
            getMissionWarehouse().get(Resource.IRON) < 10
        ) {
            return false;
        }
        return true;
    }
}
