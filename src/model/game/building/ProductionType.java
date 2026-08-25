package model.game.building;

import model.game.hex.Resource;

/*
 * Five primary production types in the game
 */
public enum ProductionType {
    LUMBER_MILL
            (BuildingType.LUMBER_MILL, Resource.WOOD, Resource.WOOD, 3, 4, 1),
    STONE_MINE
            (BuildingType.STONE_MINE, Resource.STONE, Resource.STONE, 2, 4, 2),
    IRON_MINE
            (BuildingType.IRON_MINE, Resource.IRON, Resource.IRON, 2, 4, 2),
    FARM
            (BuildingType.FARM, Resource.FOOD, Resource.FOOD, 3, 4, 1),
    STABLE
            (BuildingType.STABLE, Resource.FOOD, Resource.FOOD, 2, 5, 2),
    DOCK
            (BuildingType.DOCK, Resource.FOOD, null, 3, 7, 1);

    private final BuildingType buildingType;
    private final Resource produceResource;
    private final Resource requiredResource;
    private final int workerCapacity;
    private final int productionRate;
    private final int stationApCost;

    ProductionType(
            BuildingType buildingType,
            Resource produceResource,
            Resource requiredResource,
            int workerCapacity,
            int productionRate,
            int stationApCost
    ) {
        this.buildingType = buildingType;
        this.produceResource = produceResource;
        this.requiredResource = requiredResource;
        this.workerCapacity = workerCapacity;
        this.productionRate = productionRate;
        this.stationApCost = stationApCost;
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

    public static ProductionType fromBuildingType(BuildingType buildingType) {
        for (ProductionType productionType : values()) {
            if (productionType.getBuildingType() == buildingType) {
                return productionType;
            }
        }
        return null;
    }
}
