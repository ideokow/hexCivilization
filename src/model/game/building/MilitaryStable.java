package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.HashMap;
import java.util.Map;

public class MilitaryStable extends Building {

    public MilitaryStable(Player owner, HexCoordinate position) {
        super(BuildingType.MILITARY_STABLE, owner, position);
    }

    // TODO: implement military stable logic!

    public static Map<Resource, Integer> getConstructionCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.IRON, 2);
        cost.put(Resource.WOOD, 5);
        return cost;
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD, 1);

        if (townHall.canAfford(cost)) {
            townHall.spendResources(cost);
            resetUnpaidUpkeepTurns();
            return true;
        } else {
            increaseUnpaidUpkeepTurns();
            return false;
        }
    }
}
