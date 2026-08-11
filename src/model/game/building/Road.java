package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.HashMap;
import java.util.Map;

public class Road extends Building {

    public Road(Player owner, HexCoordinate position) {
        super(BuildingType.ROAD, owner, position);
    }

    public static Map<Resource, Integer> getConstructionCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 1);
        return cost;
    }

    public static int getConstructionAPCost() {
        return 1;
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        return true;
    }
}
