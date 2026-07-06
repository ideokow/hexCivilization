package model.game.building;

import model.game.hex.Resource;

import java.util.EnumMap;
import java.util.Map;

public enum PopulationType {
    VILLAGE(BuildingType.VILLAGE, 3, 3, cost(4, 3, 1, 0)),
    TOWN(BuildingType.TOWN, 6, 4, cost(6, 6, 2, 0));

    private final BuildingType buildingType;
    private final int unitCapIncrease;
    private final int buildApCost;
    private final Map<Resource, Integer> constructionCost;

    PopulationType(BuildingType buildingType, int unitCapIncrease, int buildApCost, Map<Resource, Integer> constructionCost) {
        this.buildingType = buildingType;
        this.unitCapIncrease = unitCapIncrease;
        this.buildApCost = buildApCost;
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

    public int getUnitCapIncrease() {
        return unitCapIncrease;
    }

    public int getBuildApCost() {
        return buildApCost;
    }

    public Map<Resource, Integer> getConstructionCost() {
        return new EnumMap<>(constructionCost);
    }

    public static PopulationType fromBuildingType(BuildingType buildingType) {
        for (PopulationType populationType : values()) {
            if (populationType.getBuildingType() == buildingType) {
                return populationType;
            }
        }
        return null;
    }
}
