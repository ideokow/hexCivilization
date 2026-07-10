package controller.game.system;

import model.game.building.TownHall;
import model.game.building.TownHallUpgrade;
import model.game.building.Upgrade;
import model.game.building.UpgradeStatus;
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

    public Upgrade checkUpgrades() {
        if (upgradeQueueTurns == 0) {
            Upgrade inQueueUpgradeCopy = inQueueUpgrade;

            // reset
            inQueueUpgrade = null;
            upgradeQueueTurns = -1;

            return inQueueUpgradeCopy;
        } else if (upgradeQueueTurns > 0) {
            upgradeQueueTurns--;
        }
        return null;
    }

    public UpgradeStatus reserveUpgrade(Upgrade upgrade) {

        if (inQueueUpgrade != null) return UpgradeStatus.UPGRADING;

        if (upgrade == Upgrade.RESOURCE) {
            UpgradeStatus upgradeStatus = townHallUpgrade.canUpgradeResourceStorage();

            if (upgradeStatus == UpgradeStatus.SUCCESS) {
                townHallUpgrade.upgradeResourceStorage();
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.STONE) {
            UpgradeStatus upgradeStatus = townHallUpgrade.canUpgradeStone();

            if (townHallUpgrade.canUpgradeStone() == UpgradeStatus.SUCCESS) {
                townHallUpgrade.upgradeStone();
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.IRON) {
            UpgradeStatus upgradeStatus = townHallUpgrade.canUpgradeIron();

            if (townHallUpgrade.canUpgradeIron() == UpgradeStatus.SUCCESS) {
                townHallUpgrade.upgradeIron();
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.TOOLS) {
            UpgradeStatus upgradeStatus = townHallUpgrade.canUpgradeTools();

            if (townHallUpgrade.canUpgradeTools() == UpgradeStatus.SUCCESS) {
                townHallUpgrade.upgradeTools();
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.TOWN) {
            UpgradeStatus upgradeStatus = townHallUpgrade.canUpgradeTown();

            if (townHallUpgrade.canUpgradeTown() == UpgradeStatus.SUCCESS) {
                townHallUpgrade.upgradeTown();
            } else {
                return upgradeStatus;
            }
        }

        inQueueUpgrade = upgrade;
        upgradeQueueTurns = upgrade.getUpgradeQueueTurns();

        return UpgradeStatus.SUCCESS;
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

    public UnitType checkGeneratorQueue() {
        if (generatorQueueTurns == 0) {
            townHall.generateUnit(inQueueUnitType);

            UnitType inQueueUnitTypeCopy = inQueueUnitType;

            // reset
            inQueueUnitType = null;
            generatorQueueTurns = -1;

            return inQueueUnitTypeCopy;
        } else if (generatorQueueTurns > 0) {
            generatorQueueTurns--;
        }
        return null;
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
