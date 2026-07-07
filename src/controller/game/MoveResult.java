package controller.game;

public enum MoveResult {
    SUCCESS,
    NOT_NEIGHBOR,        // target is not adjacent
    NOT_ENOUGH_AP,       // unit cannot pay the terrain cost
    HEX_NOT_DISCOVERED,  // cannot move into fog
    UNIT_NOT_ON_MAP,
    UNIT_IS_IN_BUILDING
}
