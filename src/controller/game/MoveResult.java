package controller.game;

public enum MoveResult {
    SUCCESS,
    NOT_NEIGHBOR,        // target is not adjacent (line 172)
    NOT_ENOUGH_AP,       // unit cannot pay the terrain cost
    HEX_OCCUPIED,        // only one unit per hex (line 132)
    HEX_NOT_DISCOVERED,  // cannot move into fog (design decision)
    UNIT_NOT_ON_MAP,
    UNIT_IS_IN_BUILDING
}
