package controller.game.system;

import controller.game.BuildingMap;
import controller.game.GameEngine;
import controller.game.UnitMap;
import model.game.building.Building;
import model.game.building.ProductionBuilding;
import model.game.townhall.TownHall;
import model.game.hex.Resource;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarvationSystem {

    public final static int eachTurnFood = 1;

    public StarvationSystem() {}

    public boolean feedUnits(TownHall townHall, UnitMap unitMap) {
        // define cost
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.FOOD, 0);

        // feed
        boolean flag = true;
        for (Unit unit : unitMap.getMap().values()) {
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

    public boolean checkStarvationStatus(TownHall townHall, BuildingMap buildingMap, UnitMap unitMap) {
        // define each cost
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.FOOD, eachTurnFood);
        // check
        return (getFoodRequirement(unitMap) > getFoodGenerationAmount(townHall, buildingMap) && !townHall.canAfford(cost));
    }

    private int getFoodRequirement(UnitMap unitMap) {
        return unitMap.getMap().size() * eachTurnFood;
    }

    private int getFoodGenerationAmount(TownHall townHall, BuildingMap buildingMap) {
        // get food sources
        List<Building> foodSources = new ArrayList<>(buildingMap.getMap().values());

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
