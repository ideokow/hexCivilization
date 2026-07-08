package controller.game.system;

import model.game.building.TownHall;
import model.game.building.TownHallUpgrade;
import model.game.building.Upgrade;

public class TownHallWaiterSystem {

    private final TownHallUpgrade townHallUpgrade;

    public TownHallWaiterSystem(TownHall townHall) {
        townHallUpgrade = townHall.getUpgrades();
    }

    private int inQueueTurns = -1;
    private Upgrade inQueueUpgrade = null;

    public void checkUpgrades() {
        if (inQueueTurns == 0) {

            if (inQueueUpgrade == Upgrade.RESOURCE) {
                townHallUpgrade.upgradeResourceStorage();
            }
            else if (inQueueUpgrade == Upgrade.STONE) {
                townHallUpgrade.upgradeStone();
            }
            else if (inQueueUpgrade == Upgrade.IRON) {
                townHallUpgrade.upgradeIron();
            }
            else if (inQueueUpgrade == Upgrade.TOOLS) {
                townHallUpgrade.upgradeTools();
            }
            else if (inQueueUpgrade == Upgrade.TOWN) {
                townHallUpgrade.upgradeTown();
            }

            // reset
            inQueueUpgrade = null;
            inQueueTurns = -1;
        } else if (inQueueTurns > 0) {
            inQueueTurns--;
        }
    }

    public boolean reserveUpgrade(Upgrade upgrade) {
        if (inQueueUpgrade == null) {
            inQueueUpgrade = upgrade;
            inQueueTurns = upgrade.getUpgradeQueueTurns();
            return true;
        }
        return false;
    }

    public Upgrade getInQueueUpgrade() {
        return inQueueUpgrade;
    }

    public int getRemainingTurns() {
        return inQueueTurns;
    }

    public boolean isThereUpgrade() {
        return !(inQueueUpgrade == null);
    }
}
