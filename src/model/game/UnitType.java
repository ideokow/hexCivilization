package model.game;

public enum UnitType {
    EXPLORER(8, 3),
    WORKER(4, 1),
    BUILDER(4, 1),
    BORDER_EXPANDER(6, 1);

    private final int eachTurnAP;
    private final int visibilityRadius;

    UnitType(int eachTurnAP, int visibilityRadius) {
        this.eachTurnAP = eachTurnAP;
        this.visibilityRadius = visibilityRadius;
    }

    public int getEachTurnAP() {
        return eachTurnAP;
    }

    public int getVisibilityRadius() {
        return visibilityRadius;
    }
}
