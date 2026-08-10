package controller.game.system;

import model.game.building.Building;
import model.game.building.ProductionBuilding;
import model.game.townhall.TownHall;
import model.game.hex.Resource;
import model.game.registry.BuildingRegistry;
import model.game.registry.UnitRegistry;
import model.game.unit.Unit;

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
        cost.put(Resource.FOOD, 0);

        // feed
        boolean flag = true;
        for (Unit unit : unitRegistry.getUnitMap().values()) {
            if (flag) {
                cost.put(Resource.FOOD, cost.get(Resource.FOOD) + eachTurnFood);
                if (!townHall.canAfford(cost)) {
                    flag = false;
                    cost.put(Resource.FOOD, cost.get(Resource.FOOD) - eachTurnFood);
                }
            } else {
                unit.spendAP(unit.getCurrentAP() / 2);
            }
        }

        townHall.spendResources(cost);
        return flag;
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
