package model.game.townhall;

import model.game.hex.Resource;

import java.util.EnumMap;
import java.util.Map;

public enum Technology {
    STONE(
            null, null,
            cost(10,  0,  0, 0), 1),
    IRON(
            Technology.STONE, null,
            cost( 0, 10,  0, 0), 1),
    GOOD_TOOLS(
            Technology.IRON, null,
            cost( 5, 10, 10, 0), 2),
    URBANIZATION(
            null, null,
            cost( 5,  5, 10, 0), 2),
    BOAT_SAILING(
            null, Level.LEVEL_2,
            cost(80,  0,  0, 0), 4),
    METALWORKING_TOOLS(
            null, Level.LEVEL_2,
            cost( 0,  0, 40, 0), 3),
    DEFENCE(
            null, Level.LEVEL_3,
            cost( 0, 100, 0, 0), 4);

    private final Technology technologyDependency;
    private final Level levelDependency;

    private final Map<Resource, Integer> acquireCost;
    private final int turnsCost;

    Technology(Technology technologyDependency, Level levelDependency, Map<Resource, Integer> cost, int turnsCost) {
        this.technologyDependency = technologyDependency;
        this.levelDependency = levelDependency;
        acquireCost = cost;
        this.turnsCost = turnsCost;
    }

    // --- Getters ---

    public Technology getTechnologyDependency() {
        return technologyDependency;
    }

    public Level getLevelDependency() {
        return levelDependency;
    }

    public Map<Resource, Integer> getAcquireCost() {
        return acquireCost;
    }

    public int getTurnsCost() {
        return turnsCost;
    }

    // --- Utils ---

    private static Map<Resource, Integer> cost(int wood, int stone, int iron, int food) {
        Map<Resource, Integer> cost = new EnumMap<>(Resource.class);
        if (wood > 0) cost.put(Resource.WOOD, wood);
        if (stone > 0) cost.put(Resource.STONE, stone);
        if (iron > 0) cost.put(Resource.IRON, iron);
        if (food > 0) cost.put(Resource.FOOD, food);
        return cost;
    }
}
