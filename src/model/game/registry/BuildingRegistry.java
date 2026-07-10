package model.game.registry;

import model.game.building.*;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.unit.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingRegistry {

    private static BuildingRegistry instance;
    public static BuildingRegistry getInstance() {
        if (instance == null) instance = new BuildingRegistry();
        return instance;
    }

    private final Map<String, Building> buildingMap;

    public BuildingRegistry() {
        buildingMap = new HashMap<>();
    }

    public Map<String, Building> getBuildingMap() {
        return buildingMap;
    }

    public void addBuilding(Building building) {
        buildingMap.put(building.getBuildingID(), building);
    }

    public UpKeepStatus payUpKeeps(TownHall townHall, HexGrid hexGrid) {

        List<Building> finishedBuildings = new ArrayList<>();
        boolean flag=true;

        // iterate over building map and pay upkeep
        for (Building building : buildingMap.values()) {
            if (!building.payUpkeep(townHall)) {
                flag = false;
                if (building.isRuined()) {
                    finishedBuildings.add(building);
                }
            }
        }

        // remove ruined buildings
        for (Building building : finishedBuildings) {
            // remove from hex
            hexGrid.get(building.getPosition()).setBuilding(null);

            // remove isWorking units
            if (building instanceof ProductionBuilding){
                List<Worker> workers = ((ProductionBuilding) building).getUnits();
                for (Worker worker : workers) {
                    worker.setInBuilding(false);
                }
            }

            // remove from registry
            buildingMap.remove(building.getBuildingID());
        }

        // return status
        if (flag) {
            return UpKeepStatus.SUCCESS;
        } else if (finishedBuildings.size() > 0) {
            return UpKeepStatus.SOME_BUILDINGS_RUINED;
        } else {
            return UpKeepStatus.UP_KEEP_PAYMENT_FAILED;
        }
    }

    public Map<Resource, Integer> generateResources(TownHall townHall) {

        Map<Resource, Integer> generatedResources = new HashMap<>();
        generatedResources.put(Resource.STONE, getGenerateResource(Resource.STONE, townHall));
        generatedResources.put(Resource.IRON, getGenerateResource(Resource.IRON, townHall));
        generatedResources.put(Resource.FOOD, getGenerateResource(Resource.FOOD, townHall));
        generatedResources.put(Resource.WOOD, getGenerateResource(Resource.WOOD, townHall));

        townHall.addResources(generatedResources);
        return generatedResources;
    }

    public int getGenerateResource(Resource resource, TownHall townHall) {
        int amount = 0;

        for (Building building : buildingMap.values()) {
            if (!building.isRuined() && building instanceof ProductionBuilding) {
                // production type
                Resource productionResource = ((ProductionBuilding) building).getProductionBuildingType().getProduceResource();

                if (resource == productionResource){
                    int productionAmount = ((ProductionBuilding) building).getProductionAmount(townHall);
                    amount += productionAmount;
                }
            }
            else if (!building.isRuined() && building instanceof TownHall) {
                // safeguard
                if (resource == Resource.FOOD || resource == Resource.WOOD) {
                    amount += TownHall.SAFE_GUARD_VALUE;
                }
            }
        }

        return amount;
    }

    public int getNetResource(Resource resource, TownHall townHall) {
        int amount = getGenerateResource(resource, townHall);

        // unit foods
        if (resource == Resource.FOOD) {
            amount -= UnitRegistry.getInstance().getUnitMap().size();
        }
        // upkeep
        else {
            for (Building building : buildingMap.values()) {
                if (building instanceof PopulationBuilding) {
                    amount -= ((PopulationBuilding) building).getPopulationType().getConstructionCost().containsKey(resource) ? 1 : 0;
                }
                else if (building instanceof ProductionBuilding) {
                    amount -= ((ProductionBuilding) building).getProductionType().getConstructionCost().containsKey(resource) ? 1 : 0;
                }
            }
        }

        return amount;
    }

    public void refreshUnitCap(TownHall townHall) {

        int towns = 0;
        int villages = 0;

        for (Building building : buildingMap.values()) {
            if (building.getType() == BuildingType.TOWN) towns++;
            else if (building.getType() == BuildingType.VILLAGE) villages++;
        }

        townHall.setUnitCap(towns, villages);
    }
}