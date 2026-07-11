package model.game.building;

public enum StationResult {
    SUCCESS
            ("Worker stationed successfully."),
    WORKER_IS_IN_ALREADY
            ("This Worker is already stationed in a building."),
    WORKER_IS_NOT_HERE
            ("The Worker must be on the building's hex."),
    ANOTHER_PLAYER_WORKER
            ("You cannot station another player's Worker."),
    BUILDING_IS_RUINED
            ("Workers cannot be stationed in a ruined building."),
    NOT_ENOUGH_STATION_AP
            ("The Worker does not have enough action points to be stationed."),
    CAPACITY_REACHED
            ("The building has reached its Worker capacity."),
    NOT_A_WORKER
            ("Only Worker units can be stationed in buildings."),
    NOT_PRODUCTION_BUILDING
            ("The selected building is not a production building."),
    WRONG_HEX
            ("Please select a valid hex for station worker operation.");

    private final String message;

    StationResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
