package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.player.Player;
import model.game.hex.Resource;
import model.game.registry.UnitRegistry;
import model.game.unit.*;

import java.util.*;

public class TownHall extends Building {

    public static final int SAFE_GUARD_VALUE = 1;

    private final HexGrid grid;

    private final Map<Resource, Integer> resourceStorage;
    private final List<Unit> units;

    private int resourceCap;
    private final int baseResourceCap = 100;

    private int unitCap;
    private final int baseUnitCap = 10;
    private int unitNumber = 0;

    public TownHall(HexGrid grid, Player owner) {

        super(BuildingType.TOWN_HALL, owner, new HexCoordinate(0, 0));
        this.grid = grid;

        resourceStorage = new HashMap<>();
        resourceStorage.put(Resource.STONE, 0);
        resourceStorage.put(Resource.IRON, 0);
        resourceStorage.put(Resource.FOOD, 0);
        resourceStorage.put(Resource.WOOD, 0);

        units = new ArrayList<>();

        resourceCap = baseResourceCap;
        unitCap = baseUnitCap;
    }

    // --- unit cap ---

    public int getUnitCap() {
        return unitCap;
    }

    public void setUnitCap(int townsNumber, int villageNumber) {
        unitCap = (int) (((double) baseUnitCap) * (((double) townsNumber * 1) + ((double) villageNumber * 0.5)));
        if (unitCap < unitNumber) unitCap = unitNumber;
    }

    // --- upgrades ---

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
        if(!canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        spendResources(cost);
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
        if(!canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        spendResources(cost);
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
        if(!canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        spendResources(cost);
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
        if(!canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        spendResources(cost);
        townUpgrade = true;
        return UpgradeStatus.SUCCESS;
    }

    public UpgradeStatus upgradeResourceStorage() {
        if (resourceStorageUpgrade >= 3) {
            return UpgradeStatus.MAXIMUM_REACHED;
        }

        Map<Resource, Integer> cost = new HashMap<>();
        cost.put(Resource.WOOD , 20);
        if(!canAfford(cost)) {
            return UpgradeStatus.NOT_ENOUGH_RESOURCE;
        }

        spendResources(cost);
        resourceStorageUpgrade++;
        resourceCap = baseResourceCap * resourceStorageUpgrade;
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

    // --- storage getter ---

    public Map<Resource, Integer> getResourceStorage() {
        return new HashMap<>(resourceStorage);
    }

    public int getStoneStorage() {
        return resourceStorage.get(Resource.STONE);
    }

    public int getIronStorage() {
        return resourceStorage.get(Resource.IRON);
    }

    public int getFoodStorage() {
        return resourceStorage.get(Resource.FOOD);
    }

    public int getWoodStorage() {
        return resourceStorage.get(Resource.WOOD);
    }

    // --- storage adders ---

    public void addStoneToStorage(int amount) {
        resourceStorage.put(Resource.STONE, resourceStorage.get(Resource.STONE) + amount);
        if (resourceStorage.get(Resource.STONE) > resourceCap) {
            resourceStorage.put(Resource.STONE, resourceCap);
        }
    }

    public void addIronToStorage(int amount) {
        resourceStorage.put(Resource.IRON, resourceStorage.get(Resource.IRON) + amount);
        if (resourceStorage.get(Resource.IRON) > resourceCap) {
            resourceStorage.put(Resource.IRON, resourceCap);
        }
    }

    public void addFoodToStorage(int amount) {
        resourceStorage.put(Resource.FOOD, resourceStorage.get(Resource.FOOD) + amount);
        if (resourceStorage.get(Resource.FOOD) > resourceCap) {
            resourceStorage.put(Resource.FOOD, resourceCap);
        }
    }

    public void addWoodToStorage(int amount) {
        resourceStorage.put(Resource.WOOD, resourceStorage.get(Resource.WOOD) + amount);
        if (resourceStorage.get(Resource.WOOD) > resourceCap) {
            resourceStorage.put(Resource.WOOD, resourceCap);
        }
    }

    // --- storage deduct ---

    public void deductStoneStorage(int amount) {
        if (amount <= getStoneStorage()) {
            resourceStorage.put(Resource.STONE, getStoneStorage() - amount);
        }
    }

    public void deductIronStorage(int amount) {
        if (amount <= getIronStorage()) {
            resourceStorage.put(Resource.IRON, getIronStorage() - amount);
        }
    }

    public void deductFoodStorage(int amount) {
        if (amount <= getFoodStorage()) {
            resourceStorage.put(Resource.FOOD, getFoodStorage() - amount);
        }
    }

    public void deductWoodStorage(int amount) {
        if (amount <= getWoodStorage()) {
            resourceStorage.put(Resource.WOOD, getWoodStorage() - amount);
        }
    }

    // Safeguard resource generator

    public void safeGuardGenerator() {
        addStoneToStorage(SAFE_GUARD_VALUE);
        addIronToStorage (SAFE_GUARD_VALUE);
    }

    // --- storage execution ---

    public boolean canAfford(Map<Resource, Integer> cost) {

        if (cost.containsKey(Resource.STONE) && getStoneStorage() < cost.get(Resource.STONE)) {
            return false;
        }
        if (cost.containsKey(Resource.IRON) && getIronStorage() < cost.get(Resource.IRON)) {
            return false;
        }
        if (cost.containsKey(Resource.FOOD) && getFoodStorage() < cost.get(Resource.FOOD)) {
            return false;
        }
        if (cost.containsKey(Resource.WOOD) && getWoodStorage() < cost.get(Resource.WOOD)) {
            return false;
        }

        return true;
    }

    public boolean spendResources(Map<Resource, Integer> cost) {
        if (!canAfford(cost)) {
            return false;
        }
        if (cost.containsKey(Resource.STONE)) {
            deductStoneStorage(cost.get(Resource.STONE));
        }
        if (cost.containsKey(Resource.IRON)) {
            deductIronStorage(cost.get(Resource.IRON));
        }
        if (cost.containsKey(Resource.FOOD)) {
            deductFoodStorage(cost.get(Resource.FOOD));
        }
        if (cost.containsKey(Resource.WOOD)) {
            deductWoodStorage(cost.get(Resource.WOOD));
        }
        return true;
    }

    public boolean addResources(Map<Resource, Integer> cost) {
        if (!canAfford(cost)) {
            return false;
        }
        if (cost.containsKey(Resource.STONE)) {
            addStoneToStorage(cost.get(Resource.STONE));
        }
        if (cost.containsKey(Resource.IRON)) {
            addIronToStorage(cost.get(Resource.IRON));
        }
        if (cost.containsKey(Resource.FOOD)) {
            addFoodToStorage(cost.get(Resource.FOOD));
        }
        if (cost.containsKey(Resource.WOOD)) {
            addWoodToStorage(cost.get(Resource.WOOD));
        }
        return true;
    }

    // --- unit list ---

    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }

    /*
    add units means add in town hall building, thus this does not impact unitNumber
     */
    public boolean addUnit(Unit unit) {
        Objects.requireNonNull(unit, "worker");
        if (isRuined() || units.contains(unit)) {
            return false;
        }
        return units.add(unit);
    }

    /*
    remove units means remove from town hall building, thus this does not impact unitNumber
     */
    public boolean removeUnit(Unit unit) {
        return units.remove(unit);
    }

    public boolean generateUnit(UnitType unitType) {

        if (unitNumber >= unitCap) return false;

        Unit unit = null;

        // make unit
        if (unitType == UnitType.WORKER) {
            unit = new Worker(getOwner(), getPosition());
            units.add(unit);
        } else if (unitType == UnitType.BUILDER) {
            unit = new Builder(getOwner(), getPosition());
            units.add(unit);
        } else if (unitType == UnitType.EXPLORER) {
            unit = new Explorer(getOwner(), getPosition());
            units.add(unit);
        } else if (unitType == UnitType.BORDER_EXPANDER) {
            unit = new BorderExpander(getOwner(), getPosition());
            units.add(unit);
        } else {
            throw new IllegalArgumentException("undefined type : " + unitType);
        }

        // full ap
        unit.resetAP();

        // add to registry
        UnitRegistry.getInstance().addUnit(unit);

        // place unit on hex
        grid.get(new HexCoordinate(0, 0)).addUnit(unit);
        unitNumber++;

        return true;
    }

    public void decreaseUnitNumber() {
        unitNumber--;
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        return true;
    }
}
