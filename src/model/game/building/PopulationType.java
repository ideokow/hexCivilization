package model.game.building;

public enum PopulationType {
    VILLAGE(BuildingType.VILLAGE, 0.5),
    TOWN(BuildingType.TOWN, 1);

    private final BuildingType buildingType;
    private final double unitCapIncrease;

    PopulationType(BuildingType buildingType, double unitCapIncrease) {
        this.buildingType = buildingType;
        this.unitCapIncrease = unitCapIncrease;
    }

    public BuildingType getBuildingType() {
        return buildingType;
    }

    public double getUnitCapIncrease() {
        return unitCapIncrease;
    }
}
