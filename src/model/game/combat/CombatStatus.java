package model.game.combat;

public enum CombatStatus {
    STARTED_SUCCESSFULLY
            ("Combat started!"),
    ALREADY_IN_COMBAT
            ("You are in combat already!"),
    TOO_MANY_UNITS
            ("There is too many military units in attack hex."),
    NO_UNITS
            ("There is not any military units in attack hex."),
    FAR_HEXES
            ("You must select two neighbor hexes or two hexes with 2 hex distance."),
    SAME_HEXES
            ("You must select two different hex!"),
    NOT_ENOUGH_AP
            ("Some of fighters does not have enough AP!"),
    NO_DEFENDER
            ("There is not any military units in defence hex.");

    private final String message;

    CombatStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
