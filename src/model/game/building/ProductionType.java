package model.game.building;

import model.game.hex.Resource;
import model.game.hex.TerrainType;

import java.util.EnumMap;
import java.util.Map;

/*
 * Five primary production types in the game
 */
public enum ProductionType {
    LUMBER_MILL
            (BuildingType.LUMBER_MILL, Resource.WOOD, Resource.WOOD, 3, 2, 1, cost(2, 0, 0, 0)),
    STONE_MINE
            (BuildingType.STONE_MINE, Resource.STONE, Resource.STONE, 2, 2, 2, cost(3, 0, 0, 0)),
    IRON_MINE
            (BuildingType.IRON_MINE, Resource.IRON, Resource.IRON, 2, 1, 2, cost(4, 1, 0, 0)),
    FARM
            (BuildingType.FARM, Resource.FOOD, Resource.FOOD, 3, 3, 1, cost(1, 0, 0, 0)),
    STABLE
            (BuildingType.STABLE, Resource.FOOD, Resource.FOOD, 2, 2, 2, cost(3, 1, 0, 0)),
    DOCK
            (BuildingType.DOCK, Resource.FOOD, null, 3, 1, 1, cost(10, 0, 4, 0));

    private final BuildingType buildingType;
    private final Resource produceResource;
    private final Resource requiredResource;
    private final int workerCapacity;
    private final int productionRate;
    private final int stationApCost;
    private final Map<Resource, Integer> constructionCost;

    ProductionType(
            BuildingType buildingType,
            Resource produceResource,
            Resource requiredResource,
            int workerCapacity,
            int productionRate,
            int stationApCost,
            Map<Resource, Integer> constructionCost
    ) {
        this.buildingType = buildingType;
        this.produceResource = produceResource;
        this.requiredResource = requiredResource;
        this.workerCapacity = workerCapacity;
        this.productionRate = productionRate;
        this.stationApCost = stationApCost;
        this.constructionCost = constructionCost;
    }

    private static Map<Resource, Integer> cost(int wood, int stone, int iron, int food) {
        Map<Resource, Integer> cost = new EnumMap<>(Resource.class);
        if (wood > 0) cost.put(Resource.WOOD, wood);
        if (stone > 0) cost.put(Resource.STONE, stone);
        if (iron > 0) cost.put(Resource.IRON, iron);
        if (food > 0) cost.put(Resource.FOOD, food);
        return cost;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public Resource getProduceResource() {
        return produceResource;
    }

    public Resource getRequiredResource() {
        return requiredResource;
    }

    public int getWorkerCapacity() {
        return workerCapacity;
    }

    public int getProductionRate() {
        return productionRate;
    }

    public int getStationApCost() {
        return stationApCost;
    }

    public Map<Resource, Integer> getConstructionCost() {
        return new EnumMap<>(constructionCost);
    }

    public static ProductionType fromBuildingType(BuildingType buildingType) {
        for (ProductionType productionType : values()) {
            if (productionType.getBuildingType() == buildingType) {
                return productionType;
            }
        }
        return null;
    }
}
