package model.game.building;

import model.game.hex.Resource;

import java.util.HashMap;
import java.util.Map;

public class TownHallUpgrade {

    private final TownHall townHall;

    public TownHallUpgrade(TownHall townHall) {
        this.townHall = townHall;
    }

    /*
    upgrade prices
    resource -> 10 wood
    stone -> 2 wood
    iron  -> 2 stone
    tools -> 1 iron, 1 stone, 1 wood
    town  -> 2 iron, 2 stone, 2 wood
     */

    private int resourceStorageUpgrade = 1;
    private boolean stoneUpgrade = false;
    private boolean ironUpgrade  = false;
    private boolean toolsUpgrade = false;
    private boolean townUpgrade  = false;

    public UpgradeStatus upgradeStone() {
        if (stoneUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD, 2);
        if(!townHall.canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        townHall.spendResources(cost);
        stoneUpgrade = true;
        return UpgradeStatus.SUCCESS;
    }

    public UpgradeStatus upgradeIron() {
        if (ironUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        if (!stoneUpgrade) {
            return UpgradeStatus.BAD_HIERARCHY;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 2);
        if(!townHall.canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        townHall.spendResources(cost);
        ironUpgrade = true;
        return UpgradeStatus.SUCCESS;
    }

    public UpgradeStatus upgradeTools() {
        if (toolsUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        if (!stoneUpgrade || !ironUpgrade) {
            return UpgradeStatus.BAD_HIERARCHY;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.IRON , 1);
        cost.put(Resource.STONE, 1);
        cost.put(Resource.WOOD , 1);
        if(!townHall.canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        townHall.spendResources(cost);
        toolsUpgrade = true;
        return UpgradeStatus.SUCCESS;
    }

    public UpgradeStatus upgradeTown() {
        if (townUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.IRON , 2);
        cost.put(Resource.STONE, 2);
        cost.put(Resource.WOOD , 2);
        if(!townHall.canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        townHall.spendResources(cost);
        townUpgrade = true;
        return UpgradeStatus.SUCCESS;
    }

    public UpgradeStatus upgradeResourceStorage() {
        if (resourceStorageUpgrade >= 3) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD , 20);
        if(!townHall.canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        townHall.spendResources(cost);
        resourceStorageUpgrade++;
        townHall.setResourceCap(townHall.getBaseResourceCap() * resourceStorageUpgrade);
        return UpgradeStatus.SUCCESS;
    }

    public boolean isStoneUpgrade() {
        return stoneUpgrade;
    }

    public boolean isIronUpgrade() {
        return ironUpgrade;
    }

    public boolean isToolsUpgrade() {
        return toolsUpgrade;
    }

    public boolean isTownUpgrade() {
        return townUpgrade;
    }

    public int getResourceStorageUpgrade() {
        return resourceStorageUpgrade;
    }
}
