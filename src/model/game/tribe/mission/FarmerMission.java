package model.game.tribe.mission;

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class FarmerMission extends Mission {

    public FarmerMission(Tribe tribe) {
        super(tribe, MissionType.FOOD_STOREHOUSE, MissionType.FOOD_STOREHOUSE.getDeadLine());
    }

    @Override
    public void payReward(TownHall townHall) {

    }

    @Override
    public boolean checkRequirements() {
        return false;
    }
}
