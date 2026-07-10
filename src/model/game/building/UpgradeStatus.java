package model.game.building;

public enum UpgradeStatus {
    SUCCESS
            ("TownHall upgrade goes in queue successfully."),
    NOT_ENOUGH_RESOURCE
            ("You do not have enough resources to upgrade this building."),
    BAD_HIERARCHY
            ("The required previous upgrade has not been completed."),
    MAXIMUM_REACHED
            ("TownHall has already reached its maximum upgrade level."),
    UPGRADING
            ("An upgrade is in queue.");

    private final String message;

    UpgradeStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
