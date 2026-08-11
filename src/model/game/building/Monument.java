package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.HashMap;
import java.util.Map;

public class Monument extends Building {
    protected Monument(Player owner, HexCoordinate position) {
        super(BuildingType.MONUMENT, owner, position);
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 10);
        cost.put(Resource.WOOD, 5);

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
