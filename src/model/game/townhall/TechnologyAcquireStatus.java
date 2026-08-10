package model.game.townhall;

public enum TechnologyAcquireStatus {
    SUCCESS
            ("Technology acquire goes in queue successfully."),
    NOT_ENOUGH_RESOURCE
            ("You do not have enough resources to acquire this technology."),
    BAD_HIERARCHY
            ("The required previous technology has not been acquired."),
    TECHNOLOGY_ACQUIRED
            ("TownHall has acquired.");

    private final String message;

    TechnologyAcquireStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
