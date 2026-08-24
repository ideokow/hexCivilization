package controller.game;

import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.ProductionBuilding;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.season.SeasonName;
import model.game.townhall.TownHall;
import model.game.unit.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingMap {

    private final Map<String, Building> map;

    public BuildingMap() {
        map = new HashMap<>();
    }

    public Map<String, Building> getMap() {
        return new HashMap<>(map);
    }

    public void addBuilding(Building building) {
        map.put(building.getBuildingID(), building);
    }

    public UpKeepStatus payUpKeeps(TownHall townHall, HexGrid hexGrid) {
        List<Building> finishedBuildings = new ArrayList<>();
        boolean flag=true;

        // iterate over building map and pay upkeep
        for (Building building : map.values()) {
            if (!building.payUpkeep(townHall)) {
                flag = false;
                if (building.isRuined()) {
                    finishedBuildings.add(building);
                }
            }
        }

        // remove ruined buildings
        for (Building building : finishedBuildings) {
            ruinBuilding(building, hexGrid);
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

    public void ruinBuilding(Building building, HexGrid hexGrid) {
        // remove from hex data
        hexGrid.get(building.getPosition()).setBuilding(null);

        // remove isWorking units
        if (building instanceof ProductionBuilding){
            List<Worker> workers = ((ProductionBuilding) building).getUnits();
            for (Worker worker : workers) {
                ((ProductionBuilding) building).removeWorker(worker);
            }
        }

        // set ruined
        building.setStateRuined();

        // remove from registry
        map.remove(building.getBuildingID());
    }

    public Map<Resource, Integer> generateResources(TownHall townHall, SeasonName season) {
        Map<Resource, Integer> generatedResources = new HashMap<>();
        generatedResources.put(Resource.STONE, getGenerateResource(Resource.STONE, townHall, season));
        generatedResources.put(Resource.IRON,  getGenerateResource(Resource.IRON,  townHall, season));
        generatedResources.put(Resource.FOOD,  getGenerateResource(Resource.FOOD,  townHall, season));
        generatedResources.put(Resource.WOOD,  getGenerateResource(Resource.WOOD,  townHall, season));

        townHall.addResources(generatedResources);
        return generatedResources;
    }

    public int getGenerateResource(Resource resource, TownHall townHall, SeasonName season) {
        int seasonBonus = 0;
        if (resource.equals(Resource.FOOD)) {
            if (season.equals(SeasonName.SPRING)) seasonBonus =  1;
            if (season.equals(SeasonName.WINTER)) seasonBonus = -1;
        }

        int amount = 0;

        for (Building building : map.values()) {
            if (!building.isRuined() && building instanceof ProductionBuilding) {
                // production type
                Resource productionResource = ((ProductionBuilding) building).getProductionBuildingType().getProduceResource();

                if (resource == productionResource){
                    int productionAmount = ((ProductionBuilding) building).getProductionAmount(townHall);
                    amount += productionAmount + seasonBonus;
                }
            }
            else if (!building.isRuined() && building instanceof TownHall) {
                // safeguard
                if (resource == Resource.FOOD || resource == Resource.WOOD) {
                    amount += TownHall.SAFE_GUARD_VALUE + seasonBonus;
                }
            }
        }

        return amount;
    }

    public int getNetResource(Resource resource, TownHall townHall, SeasonName season) {
        int amount = getGenerateResource(resource, townHall, season);

        // unit foods
        if (resource == Resource.FOOD) {
            amount -= GameEngine.getInstance().getUnitMap().getMap().size();
        }
        // upkeep
        for (Building building : map.values()) {
            amount -= building.getType().getUpkeepCost().getOrDefault(resource, 0);
        }

        return amount;
    }

    public void refreshUnitCap(TownHall townHall) {
        int towns = 0;
        int villages = 0;

        for (Building building : map.values()) {
            if (building.getType() == BuildingType.TOWN) towns++;
            else if (building.getType() == BuildingType.VILLAGE) villages++;
        }

        townHall.setUnitCap(towns, villages);
    }
}
