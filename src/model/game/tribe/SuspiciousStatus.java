package model.game.tribe;

public enum SuspiciousStatus {
    ITS_OK
            (""),
    IS_SUSPICIOUS
            ("Be careful! The tribe is suspicious of you."),
    IS_ENEMY
            ("");

    private final String message;

    SuspiciousStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
