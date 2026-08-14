package model.game.townhall.opration;

public enum GenerationStatus {
    UNIT_CAP_REACHED
            ("Unit cap reached!"),
    MILITARY_UNIT_CAP_REACHED
            ("Military unit cap reached!"),
    CANT_BUILD_CAVALRY
            ("You cant generate cavalry in TownHall."),
    CANT_AFFORD_COST
            ("There is not enough resource for unit generation."),
    NOT_ENOUGH_LEVEL
            ("Not enough level for generation."),
    SUCCESS
            ("Unit generation goes to queue successfully.");

    private final String message;

    GenerationStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
