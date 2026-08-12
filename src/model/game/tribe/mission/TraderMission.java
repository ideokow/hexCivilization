package model.game.tribe.mission;

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public class TraderMission extends Mission {

    public TraderMission(Tribe tribe) {
        super(tribe, MissionType.TRADE_ROUTE, MissionType.TRADE_ROUTE.getDeadLine());
    }

    @Override
    public void payReward(TownHall townHall) {

    }

    @Override
    public boolean checkRequirements() {
        return false;
    }
}
