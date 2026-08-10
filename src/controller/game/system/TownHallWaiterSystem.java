package controller.game.system;

import model.game.townhall.*;
import model.game.unit.UnitType;

/*
this class manages town hall queues such as upgrade queue and unit generator
 */
public class TownHallWaiterSystem {

    private final TownHall townHall;
    private final Technologies technologies;

    public TownHallWaiterSystem(TownHall townHall) {
        this.townHall = townHall;
        technologies = townHall.getTechnologies();
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

    public TechnologyAcquireStatus reserveUpgrade(Upgrade upgrade) {

        if (inQueueUpgrade != null) return TechnologyAcquireStatus.UPGRADING;

        else if (upgrade == Upgrade.STONE) {
            TechnologyAcquireStatus upgradeStatus = technologies.canAcquire(Technology.STONE);

            if (technologies.canAcquire(Technology.STONE) == TechnologyAcquireStatus.SUCCESS) {
                technologies.acquire(Technology.STONE);
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.IRON) {
            TechnologyAcquireStatus upgradeStatus = technologies.canAcquire(Technology.IRON);

            if (technologies.canAcquire(Technology.IRON) == TechnologyAcquireStatus.SUCCESS) {
                technologies.acquire(Technology.IRON);
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.TOOLS) {
            TechnologyAcquireStatus upgradeStatus = technologies.canAcquire(Technology.GOOD_TOOLS);

            if (technologies.canAcquire(Technology.GOOD_TOOLS) == TechnologyAcquireStatus.SUCCESS) {
                technologies.acquire(Technology.GOOD_TOOLS);
            } else {
                return upgradeStatus;
            }
        }

        else if (upgrade == Upgrade.TOWN) {
            TechnologyAcquireStatus upgradeStatus = technologies.canAcquire(Technology.URBANIZATION);

            if (technologies.canAcquire(Technology.URBANIZATION) == TechnologyAcquireStatus.SUCCESS) {
                technologies.acquire(Technology.URBANIZATION);
            } else {
                return upgradeStatus;
            }
        }

        inQueueUpgrade = upgrade;
        upgradeQueueTurns = upgrade.getUpgradeQueueTurns();

        return TechnologyAcquireStatus.SUCCESS;
    }

    public Upgrade getInQueueUpgrade() {
        return inQueueUpgrade;
    }

    public int getUpgradeRemainingTurns() {
        return upgradeQueueTurns;
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

    public GenerationReserveStatus reserveGeneration(UnitType unitType) {
        if (inQueueUnitType == null) {

            if ((new StarvationSystem(townHall)).checkStarvationStatus()) {
                return GenerationReserveStatus.STARVATION;
            }

            inQueueUnitType = unitType;
            generatorQueueTurns = requiredGeneratorQueueTurns;
            return GenerationReserveStatus.SUCCESS;
        }
        return GenerationReserveStatus.GENERATION_IN_QUEUE;
    }

    public int getGenerationRemainingTurns() {
        return generatorQueueTurns;
    }

    public UnitType getInQueueUnitType() {
        return inQueueUnitType;
    }
}
