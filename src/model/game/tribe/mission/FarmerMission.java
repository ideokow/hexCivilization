package model.game.tribe.mission;

import model.game.hex.Resource;
import model.game.tribe.Tribe;

public class FarmerMission extends Mission {

    public FarmerMission(Tribe tribe) {
        super(tribe, MissionType.FOOD_STOREHOUSE);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(15);
        getTribe().getRelatedTownHall().addFoodToStorage(30);
    }

    @Override
    public boolean checkRequirements() {
        if (
            !getMissionWarehouse().containsKey(Resource.WOOD) ||
            getMissionWarehouse().get(Resource.WOOD) < 20
        ) {
            return false;
        }
        if (
            !getMissionWarehouse().containsKey(Resource.STONE) ||
            getMissionWarehouse().get(Resource.STONE) < 10
        ) {
            return false;
        }
        return true;
    }
}
