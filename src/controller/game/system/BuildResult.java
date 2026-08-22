package controller.game.system;

public enum BuildResult {
    SUCCESS
            ("Building constructed successfully."),
    NOT_A_BUILDER
            ("Only Builder units can construct buildings."),
    BUILDER_NOT_ON_HEX
            ("The Builder must stand on the target hex."),
    OUTSIDE_TERRITORY
            ("Buildings can only be constructed inside your territory."),
    HEX_NOT_DISCOVERED
            ("The target hex has not been discovered yet."),
    HEX_HAS_BUILDING
            ("The target hex already contains a building."),
    EDGE_HAS_WALL
            ("The target edge already contains a wall."),
    WRONG_TERRAIN
            ("This building cannot be constructed on the selected terrain."),
    MISSING_HEX_RESOURCE
            ("The target hex does not contain the required resource."),
    NOT_ENOUGH_RESOURCES
            ("You do not have enough resources to construct this building."),
    NOT_ENOUGH_AP
            ("The Builder does not have enough action points."),
    NOT_ENOUGH_CHARGE
            ("The Builder does not have enough build charges."),
    UPGRADE_REQUIRED
            ("The required upgrade has not been unlocked."),
    CANT_BUILD_TOWN_HALL
            ("A Town Hall cannot be constructed directly."),
    CANT_BUILD_TRADING_POST
            ("A Trading Post is a neutral map structure and cannot be constructed."),
    NULL_ARGUMENTS
            ("Error: Objects not on the map!"),
    NO_WATER_FOR_DOCK
            ("You should build Dock across water."),
    LEVEL_REQUIREMENT
            ("You don't have enough level requirements.");

    private final String message;

    BuildResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
