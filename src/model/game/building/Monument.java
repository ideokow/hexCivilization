package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.HashMap;
import java.util.Map;

public class Monument extends Building {
    public Monument(Player owner, HexCoordinate position) {
        super(BuildingType.MONUMENT, owner, position);
    }

    public static Map<Resource, Integer> getConstructionCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 10);
        cost.put(Resource.WOOD, 5);
        return cost;
    }

    public static int getConstructionAPCost() {
        return 5;
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 1);
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
