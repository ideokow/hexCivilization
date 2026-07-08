package controller.game.system;

import model.game.building.TownHall;
import model.game.building.TownHallUpgrade;
import model.game.building.Upgrade;
import model.game.unit.UnitType;

/*
this class manages town hall queues such as upgrade queue and unit generator
 */
public class TownHallWaiterSystem {

    private final TownHall townHall;
    private final TownHallUpgrade townHallUpgrade;

    public TownHallWaiterSystem(TownHall townHall) {
        this.townHall = townHall;
        townHallUpgrade = townHall.getUpgrades();
    }

    // upgrade queue
    private int upgradeQueueTurns = -1;
    private Upgrade inQueueUpgrade = null;

    // generator queue
    private int generatorQueueTurns = -1;
    private UnitType inQueueUnitType = null;

    public final static int requiredGeneratorQueueTurns = 3;

    // --- upgrade functions ---

    public void checkUpgrades() {
        if (upgradeQueueTurns == 0) {

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
            upgradeQueueTurns = -1;
        } else if (upgradeQueueTurns > 0) {
            upgradeQueueTurns--;
        }
    }

    public boolean reserveUpgrade(Upgrade upgrade) {
        if (inQueueUpgrade == null) {
            inQueueUpgrade = upgrade;
            upgradeQueueTurns = upgrade.getUpgradeQueueTurns();
            return true;
        }
        return false;
    }

    public Upgrade getInQueueUpgrade() {
        return inQueueUpgrade;
    }

    public int getUpgradeRemainingTurns() {
        return upgradeQueueTurns;
    }

    public boolean isThereUpgrade() {
        return !(inQueueUpgrade == null);
    }

    // --- generator functions ---

    public void checkGeneratorQueue() {
        if (generatorQueueTurns == 0) {
            townHall.generateUnit(inQueueUnitType);

            // reset
            inQueueUnitType = null;
            generatorQueueTurns = -1;
        } else if (generatorQueueTurns > 0) {
            generatorQueueTurns--;
        }
    }

    public boolean reserveGeneration(UnitType unitType) {
        if (inQueueUnitType == null) {
            inQueueUnitType = unitType;
            generatorQueueTurns = requiredGeneratorQueueTurns;
            return true;
        }
        return false;
    }

    public int getGenerationRemainingTurns() {
        return generatorQueueTurns;
    }

    public boolean isThereGeneration() {
        return !(inQueueUnitType == null);
    }
}
