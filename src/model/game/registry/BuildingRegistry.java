package model.game.registry;

import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.ProductionBuilding;
import model.game.building.TownHall;
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

    public boolean payUpKeeps(TownHall townHall, HexGrid hexGrid) {

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

        return flag;
    }

    public Map<Resource, Integer> generateResources(TownHall townHall) {

        Map<Resource, Integer> allGeneratedResources = new HashMap<>();
        allGeneratedResources.put(Resource.STONE, 0);
        allGeneratedResources.put(Resource.IRON, 0);
        allGeneratedResources.put(Resource.FOOD, 0);
        allGeneratedResources.put(Resource.WOOD, 0);

        for (Building building : buildingMap.values()) {
            if (!building.isRuined() && building instanceof ProductionBuilding) {
                // production type
                Resource resource = ((ProductionBuilding) building).getProductionBuildingType().getProduceResource();
                int productionAmount = ((ProductionBuilding) building).getProductionAmount(townHall);

                // apply in town hall
                Map<Resource, Integer> generatedResource = new HashMap<>();
                generatedResource.put(resource, productionAmount);
                townHall.addResources(generatedResource);

                // add to statistics
                allGeneratedResources.put(resource, allGeneratedResources.get(resource) + productionAmount);
            }
            else if (!building.isRuined() && building instanceof TownHall) {
                // safeguard
                ((TownHall) building).safeGuardGenerator();
            }
        }

        return allGeneratedResources;
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