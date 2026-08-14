package model.game.building;

import model.game.hex.TerrainType;

import java.util.EnumSet;
import java.util.Set;

public enum BuildingType {

    TOWN_HALL(
            -1,
            Set.of(TerrainType.TOWN_HALL), Set.of() // technically it's useless
    ),
    LUMBER_MILL(
            1,
            Set.of(TerrainType.FOREST), Set.of()
    ),
    STONE_MINE(
            2,
            Set.of(TerrainType.MOUNTAIN), Set.of()
    ),
    IRON_MINE(
            2,
            Set.of(TerrainType.MOUNTAIN), Set.of()
    ),
    FARM(
            1,
            Set.of(TerrainType.GRASSLAND), Set.of()
    ),
    STABLE(
            2,
            Set.of(TerrainType.PLAINS), Set.of()
    ),
    MILITARY_STABLE(
            2,
            Set.of(TerrainType.PLAINS), Set.of()
    ),
    DOCK(
            3,
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    VILLAGE(
            3,
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    TOWN(
            4,
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    MONUMENT(
            5,
            Set.of(), Set.of(TerrainType.SEA, TerrainType.RIVER, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    ),
    ROAD(
            1,
            Set.of(), Set.of(TerrainType.SEA, TerrainType.MOUNTAIN_RANGE, TerrainType.TOWN_HALL)
    );

    private final int buildAPCost;
    private final Set<TerrainType> allowed; // whitelist
    private final Set<TerrainType> blocked; // blacklist

    BuildingType(int buildAPCost, Set<TerrainType> allowed, Set<TerrainType> blocked) {
        this.buildAPCost = buildAPCost;
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
}
