package model.game.townhall;

import model.game.hex.Resource;

import java.util.EnumMap;
import java.util.Map;

public enum Level {
    LEVEL_1(200, 100, cost( 0,   0,  0, 0)),
    LEVEL_2(250, 200, cost(50,  50,  0, 0)),
    LEVEL_3(300, 300, cost( 0, 100, 50, 0));

    private final int baseMaximumHP;
    private final int resourceCap;
    private final Map<Resource, Integer> cost;

    Level(int baseMaximumHP, int resourceCap, Map<Resource, Integer> cost) {
        this.baseMaximumHP = baseMaximumHP;
        this.resourceCap = resourceCap;
        this.cost = cost;
    }

    // --- Getters ---

    public int getBaseMaximumHP() {
        return baseMaximumHP;
    }

    public int getResourceCap() {
        return resourceCap;
    }

    public Map<Resource, Integer> getCost() {
        return cost;
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
