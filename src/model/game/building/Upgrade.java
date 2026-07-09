package model.game.building;

public enum Upgrade {
    RESOURCE(5, "Resource"),
    STONE(3, "Stone"),
    IRON(5, "Iron"),
    TOOLS(7, "Tools"),
    TOWN(4, "Town");

    private final int upgradeQueueTurns;
    private final String name;

    Upgrade(int upgradeQueueTurns, String name) {
        this.upgradeQueueTurns = upgradeQueueTurns;
        this.name = name;
    }

    public int getUpgradeQueueTurns() {
        return upgradeQueueTurns;
    }

    public String getName() {
        return name;
    }
}
