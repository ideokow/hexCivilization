package model.game.tribe.mission;

public enum MissionType {
    FOOD_STOREHOUSE(1),
    TRADE_ROUTE(1),
    MILITARY_ASSISTANCE(1),
    MINING_TOOLS(1),
    COASTAL_DEVELOPMENT(1);

    private final int deadLine;

    MissionType(int deadLine) {
        this.deadLine = deadLine;
    }

    public int getDeadLine() {
        return deadLine;
    }
}