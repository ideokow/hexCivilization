package controller.game;

public enum Disaster {
    EARTH_QUAKE
            ("An earth quake just happened!"),
    FLOOD
            ("A flood just happened!"),
    BEAR_ATTACK
            ("A bear just attacked!");

    private final String message;

    Disaster(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
