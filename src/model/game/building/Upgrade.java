package model.game.building;

public enum Upgrade {
    RESOURCE(5),
    STONE(3),
    IRON(5),
    TOOLS(7),
    TOWN(4);

    private final int upgradeQueueTurns;

    Upgrade(int upgradeQueueTurns) {
        this.upgradeQueueTurns = upgradeQueueTurns;
    }

    public int getUpgradeQueueTurns() {
        return upgradeQueueTurns;
    }
}
