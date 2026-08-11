package model.game.hex;

/*
terrain types and their movement costs
 */
public enum TerrainType {

    TOWN_HALL(0),
    PLAINS(1),
    GRASSLAND(1),
    FOREST(2),
    MOUNTAIN(4),
    SEA(1),
    RIVER(5),
    MOUNTAIN_RANGE(-1); // impossible to go through

    private final int movementCost;

    TerrainType(int movementCost) {
        this.movementCost = movementCost;
    }

    public int getMovementCost() {
        return movementCost;
    }
}
