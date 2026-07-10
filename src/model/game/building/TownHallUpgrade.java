package model.game.building;

import model.game.hex.Resource;

import java.util.HashMap;
import java.util.Map;

public class TownHallUpgrade {

    private final TownHall townHall;

    private int resourceStorageUpgrade = 1;
    private boolean stoneUpgrade = false;
    private boolean ironUpgrade = false;
    private boolean toolsUpgrade = false;
    private boolean townUpgrade = false;

    public TownHallUpgrade(TownHall townHall) {
        this.townHall = townHall;
    }

    public UpgradeStatus canUpgradeStone() {
        if (stoneUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }
        else if (townHall.canAfford(getStoneUpgradeCost())) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }
        else {
            return UpgradeStatus.SUCCESS;
        }
    }

    public void upgradeStone() {
        if (!(canUpgradeStone() == UpgradeStatus.SUCCESS)) return;

        townHall.spendResources(getStoneUpgradeCost());
        stoneUpgrade = true;
    }

    public UpgradeStatus canUpgradeIron() {
        if (!stoneUpgrade) {
            return UpgradeStatus.BAD_HIERARCHY;
        }
        else if (ironUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }
        else if (townHall.canAfford(getIronUpgradeCost())) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }
        else {
            return UpgradeStatus.SUCCESS;
        }
    }

    public void upgradeIron() {
        if (!(canUpgradeTools() == UpgradeStatus.SUCCESS)) return;

        townHall.spendResources(getIronUpgradeCost());
        ironUpgrade = true;
    }

    public UpgradeStatus canUpgradeTools() {
        if (!ironUpgrade) {
            return UpgradeStatus.BAD_HIERARCHY;
        }
        else if (toolsUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }
        else if (townHall.canAfford(getToolsUpgradeCost())) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }
        else {
            return UpgradeStatus.SUCCESS;
        }
    }

    public void upgradeTools() {
        if (!(canUpgradeTools() == UpgradeStatus.SUCCESS)) return;

        townHall.spendResources(getToolsUpgradeCost());
        toolsUpgrade = true;
    }

    public UpgradeStatus canUpgradeTown() {
        if (townUpgrade) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }
        else if (townHall.canAfford(getTownUpgradeCost())) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }
        else {
            return UpgradeStatus.SUCCESS;
        }
    }

    public void upgradeTown() {
        if (!(canUpgradeTown() == UpgradeStatus.SUCCESS)) return;

        townHall.spendResources(getTownUpgradeCost());
        townUpgrade = true;
    }

    public UpgradeStatus canUpgradeResourceStorage() {
        if (resourceStorageUpgrade > 3) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }
        else if (townHall.canAfford(getResourceStorageUpgradeCost())) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }
        else {
            return UpgradeStatus.SUCCESS;
        }
    }

    public void upgradeResourceStorage() {
        if (!(canUpgradeResourceStorage() == UpgradeStatus.SUCCESS)) return;

        townHall.spendResources(getResourceStorageUpgradeCost());
        resourceStorageUpgrade++;
        townHall.setResourceCap(
                townHall.getBaseResourceCap() * resourceStorageUpgrade
        );
    }

    private Map<Resource, Integer> getStoneUpgradeCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD, 2);
        return cost;
    }

    private Map<Resource, Integer> getIronUpgradeCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.STONE, 2);
        return cost;
    }

    private Map<Resource, Integer> getToolsUpgradeCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.IRON, 1);
        cost.put(Resource.STONE, 1);
        cost.put(Resource.WOOD, 1);
        return cost;
    }

    private Map<Resource, Integer> getTownUpgradeCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.IRON, 2);
        cost.put(Resource.STONE, 2);
        cost.put(Resource.WOOD, 2);
        return cost;
    }

    private Map<Resource, Integer> getResourceStorageUpgradeCost() {
        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD, 20);
        return cost;
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
