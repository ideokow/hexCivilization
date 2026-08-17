package model.game.tribe;

import model.game.hex.Resource;

import java.util.EnumMap;
import java.util.Map;

public enum TribeType {
    FARMER
        (resourcePack(0, 0, 0, 2), 2),
    FIGHTER
        (resourcePack(0, 0, 0, 0), 5),
    TRADER
        (resourcePack(1, 0, 1, 0), 1),
    MOUNTAINEER
        (resourcePack(0, 2, 0, 0), 2),
    COASTAL
        (resourcePack(0, 0, 0, 2), 2);

    private final Map<Resource, Integer> reward;
    private final int militaryCap;

    TribeType(Map<Resource, Integer> reward, int cap) {
        this.reward = reward;
        militaryCap = cap;
    }

    Map<Resource, Integer> getRelatedReward() {
        return reward;
    }

    public int getMilitaryCap() {
        return militaryCap;
    }

    private static Map<Resource, Integer> resourcePack(int wood, int stone, int iron, int food) {
        Map<Resource, Integer> cost = new EnumMap<>(Resource.class);
        if (wood > 0) cost.put(Resource.WOOD, wood);
        if (stone > 0) cost.put(Resource.STONE, stone);
        if (iron > 0) cost.put(Resource.IRON, iron);
        if (food > 0) cost.put(Resource.FOOD, food);
        return cost;
    }
}
