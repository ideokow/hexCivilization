package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.Map;

public class PopulationBuilding extends Building {

    private PopulationType populationType;

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

    public PopulationType getPopulationType() {
        return populationType;
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
            resetUnpaidUpkeepTurns();
            return true;
        } else {
            increaseUnpaidUpkeepTurns();
            return false;
        }
    }
}
