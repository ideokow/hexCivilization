package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;

import java.util.Map;

public class PopulationBuilding extends Building {

    private PopulationType populationType = null;

    public PopulationBuilding(Player owner, BuildingType type, HexCoordinate position) {
        super(type, owner, position);

        if (type == BuildingType.VILLAGE) {
            populationType = PopulationType.VILLAGE;
        }
        else if (type == BuildingType.TOWN) {
            populationType = PopulationType.TOWN;
        }
        else {
            throw new IllegalArgumentException("Population building type must be VILLAGE or TOWN");
        }
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        /*
        One unit of each material required for maintenance cost
         */
        Map<Resource, Integer> cost = populationType.getConstructionCost();
        for (Resource resource : cost.keySet()) {
            cost.put(resource, 1);
        }

        if (townHall.canAfford(cost)) {
            townHall.spendResources(cost);
            return true;
        } else {
            increaseUnpaidUpkeepTurns();
            return false;
        }
    }
}
