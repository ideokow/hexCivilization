package model.game.hex;

/*
terrain types and their movement costs
 */
public enum TerrainType {

    TOWN_HALL
            (0, true),
    PLAINS
            (1, true),
    GRASSLAND
            (1, true),
    FOREST
            (2, true),
    MOUNTAIN
            (4, true),
    SEA
            (1, false),
    RIVER
            (5, false),
    MOUNTAIN_RANGE
            (-1, true); // impossible to go through

    private final int movementCost;
    private final boolean land;

    TerrainType(int movementCost, boolean land) {
        this.movementCost = movementCost;
        this.land = land;
    }

    public int getMovementCost() {
        return movementCost;
    }

    public boolean isLand() {
        return land;
    }
}
