package controller.game.system;

import model.game.building.Building;
import model.game.building.ProductionBuilding;
import model.game.building.TownHall;
import model.game.hex.Resource;
import model.game.registry.BuildingRegistry;
import model.game.registry.UnitRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarvationSystem {

    public final static int eachTurnFood = 1;

    private final TownHall townHall;
    private final UnitRegistry unitRegistry;

    public StarvationSystem(TownHall townHall) {
        this.townHall = townHall;
        unitRegistry = UnitRegistry.getInstance();
    }

    public boolean feedUnits() {
        // define cost
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.FOOD, getFoodRequirement());

        // check storage then pay
        if (townHall.canAfford(cost)) {
            townHall.spendResources(cost);
            return true;
        } else {
            return false;
        }
    }

    public boolean checkStarvationStatus() {
        // define each cost
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.FOOD, eachTurnFood);
        // check
        return (getFoodRequirement() > getFoodGenerationAmount() && !townHall.canAfford(cost));
    }

    private int getFoodRequirement() {
        return unitRegistry.getUnitMap().size() * eachTurnFood;
    }

    private int getFoodGenerationAmount() {
        // get food sources
        List<Building> foodSources = new ArrayList<>(BuildingRegistry.getInstance().getBuildingMap().values());

        int sourceAmount = 0;
        for (Building foodSource : foodSources) {
            if (foodSource instanceof ProductionBuilding &&
                    ((ProductionBuilding) foodSource).getProductionType().getProduceResource() == Resource.FOOD) {
                sourceAmount += ((ProductionBuilding) foodSource).getProductionAmount(townHall);
            }
            else if (foodSource instanceof TownHall) {
                sourceAmount += TownHall.SAFE_GUARD_VALUE;
            }
        }

        return sourceAmount;
    }
}
