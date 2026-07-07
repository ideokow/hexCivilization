package controller.game.system;

public enum BuildResult {
    SUCCESS,
    NOT_A_BUILDER,        // only Builder can construct
    BUILDER_NOT_ON_HEX,   // builder must stand on the target hex
    OUTSIDE_TERRITORY,    // must be inside player borders
    HEX_NOT_DISCOVERED,   // hex not discovered
    HEX_HAS_BUILDING,     // hex must be clear
    WRONG_TERRAIN,        // e.g. Lumber Mill off forest
    MISSING_HEX_RESOURCE, // e.g. Iron Mine without iron
    HEX_HAS_RESOURCE,     // Town only on resource-free hexes
    NOT_ENOUGH_RESOURCES,
    NOT_ENOUGH_AP,
    NOT_ENOUGH_CHARGE,
    UPGRADE_REQUIRED,    // Stone/Iron Mine tech & Town/Village
    CANT_BUILD_TOWN_HALL,
    UNIT_NOT_ON_MAP
}
