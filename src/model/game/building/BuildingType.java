package model.game.building;

import model.game.hex.Resource;
import model.game.hex.TerrainType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum BuildingType {

    TOWN_HALL( // player can't build this
            -1,
            cost(0, 0, 0, 0),
            cost(0, 0, 0, 0),
            Set.of(TerrainType.TOWN_HALL), Set.of()
    ),
    LUMBER_MILL(
            1,
            cost(2, 0, 0, 0),
            cost(1, 0, 0, 0),
            Set.of(TerrainType.FOREST), Set.of()
    ),
    STONE_MINE(
            2,
            cost(3, 0, 0, 0),
            cost(1, 0, 0, 0),
            Set.of(TerrainType.MOUNTAIN), Set.of()
    ),
    IRON_MINE(
            2,
            cost(4, 1, 0, 0),
            cost(1, 0, 0, 0),
            Set.of(TerrainType.MOUNTAIN), Set.of()
    ),
    FARM(
            1,
            cost(1, 0, 0, 0),
            cost(0, 0, 0, 0),
            Set.of(TerrainType.GRASSLAND), Set.of()
    ),
    STABLE(
            2,
            cost(3, 0, 0, 0),
            cost(1, 0, 0, 0),
            Set.of(TerrainType.PLAINS), Set.of()
    ),
    MILITARY_STABLE(
            2,
            cost(5, 0, 2, 0),
            cost(1, 0, 0, 0),
            Set.of(TerrainType.PLAINS), Set.of()
    ),
    DOCK(
            3,
            cost(10, 0, 4, 0),
            cost(2, 0, 1, 0),
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    VILLAGE(
            3,
            cost(4, 3, 1, 0),
            cost(1, 1, 0, 1),
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    TOWN(
            4,
            cost(6, 6, 2, 0),
            cost(1, 1, 1, 1),
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    MONUMENT(
            5,
            cost(5, 10, 0, 0),
            cost(1, 1, 0, 0),
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    ROAD(
            1,
            cost(0, 1, 0, 0),
            cost(0, 0, 0, 0),
            Set.of(), Set.of(TerrainType.SEA, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    TRIBE_CAMP( // player can't build this
            -1,
            cost(0, 0, 0, 0),
            cost(0, 0, 0, 0),
            Set.of(), Set.of()
    );

    private final int buildAPCost;
    private final Map<Resource, Integer> constructionCost;
    private final Map<Resource, Integer> upkeepCost;
    private final Set<TerrainType> allowed; // whitelist
    private final Set<TerrainType> blocked; // blacklist

    BuildingType(int buildAPCost, Map<Resource, Integer> constructionCost, Map<Resource, Integer> upkeepCost,
                 Set<TerrainType> allowed, Set<TerrainType> blocked) {
        this.buildAPCost = buildAPCost;
        this.constructionCost = constructionCost;
        this.upkeepCost = upkeepCost;
        this.allowed = allowed.isEmpty()
                ? EnumSet.allOf(TerrainType.class)
                : EnumSet.copyOf(allowed);
        this.blocked = blocked.isEmpty()
                ? EnumSet.noneOf(TerrainType.class)
                : EnumSet.copyOf(blocked);
    }

    public int getBuildAPCost() {
        return buildAPCost;
    }

    public Map<Resource, Integer> getConstructionCost() {
        return constructionCost;
    }

    public Map<Resource, Integer> getUpkeepCost() {
        return upkeepCost;
    }

    public boolean isAllowedOn(TerrainType terrain) {
        if (terrain == null) return false;
        if (allowed != null) {
            return allowed.contains(terrain);
        }
        else if (blocked != null) {
            return !blocked.contains(terrain);
        }
        else return true;
    }

    private static Map<Resource, Integer> cost(int wood, int stone, int iron, int food) {
        Map<Resource, Integer> cost = new EnumMap<>(Resource.class);
        if (wood > 0) cost.put(Resource.WOOD, wood);
        if (stone > 0) cost.put(Resource.STONE, stone);
        if (iron > 0) cost.put(Resource.IRON, iron);
        if (food > 0) cost.put(Resource.FOOD, food);
        return cost;
    }
}
