package model.game.unit;

public enum UnitType {
    EXPLORER(8, 3, "Explorer"),
    WORKER(4, 1, "Worker"),
    BUILDER(5, 1, "Builder"),
    BORDER_EXPANDER(6, 1, "Border Expander");

    private final int eachTurnAP;
    private final int visibilityRadius;
    private final String name;

    UnitType(int eachTurnAP, int visibilityRadius, String name) {
        this.eachTurnAP = eachTurnAP;
        this.visibilityRadius = visibilityRadius;
        this.name = name;
    }

    public int getEachTurnAP() {
        return eachTurnAP;
    }

    public int getVisibilityRadius() {
        return visibilityRadius;
    }

    public String getName() {
        return name;
    }
}
