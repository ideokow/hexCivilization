package model.game.building;

import model.game.hex.Resource;

/*
 * Five primary production types in the game
 */
public enum ProductionType {
    LUMBER_MILL(BuildingType.LUMBER_MILL, Resource.WOOD),
    STONE_MINE(BuildingType.STONE_MINE, Resource.STONE),
    IRON_MINE(BuildingType.IRON_MINE, Resource.IRON),
    FARM(BuildingType.FARM, Resource.FOOD),
    STABLE(BuildingType.STABLE, Resource.FOOD);

    private final BuildingType buildingType;
    private final Resource produceResource;

    ProductionType(BuildingType buildingType, Resource produceResource) {
        this.buildingType = buildingType;
        this.produceResource = produceResource;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public Resource getProduceResource() {
        return produceResource;
    }
}
