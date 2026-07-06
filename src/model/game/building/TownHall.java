package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.hex.Resource;
import model.game.unit.*;

import java.util.*;

public class TownHall extends Building {

    public static final int SAFE_GUARD_VALUE = 1;

    private final Map<Resource, Integer> resourceStorage;
    private final List<Unit> units;

    private int unitCap = 10;
    private int unitNumber;

    public TownHall(Player owner, List<Unit> initialUnits) {

        super(BuildingType.TOWN_HALL, owner, new HexCoordinate(0, 0));

        resourceStorage = new HashMap<>();
        resourceStorage.put(Resource.STONE, 0);
        resourceStorage.put(Resource.IRON , 0);
        resourceStorage.put(Resource.FOOD , 0);
        resourceStorage.put(Resource.WOOD , 0);

        units = new ArrayList<>(initialUnits);
        unitNumber = initialUnits.size();

        if (unitNumber > unitCap) throw new IllegalArgumentException("too many initial units");
    }

    // --- unit cap ---

    public int getUnitCap() {
        return unitCap;
    }

    public void setUnitCap(int unitCap) {
        this.unitCap = unitCap;
    }

    // --- upgrades ---

    private int resourceStorageUpgrade = 0;
    private boolean stoneUpgrade = false;
    private boolean ironUpgrade  = false;
    private boolean toolsUpgrade = false;
    private boolean townUpgrade  = false;

    public void setStoneUpgrade(boolean stoneUpgrade) {
        this.stoneUpgrade = stoneUpgrade;
    }

    public void setIronUpgrade(boolean ironUpgrade) {
        this.ironUpgrade = ironUpgrade;
    }

    public void setToolsUpgrade(boolean toolsUpgrade) {
        this.toolsUpgrade = toolsUpgrade;
    }

    public void setTownUpgrade(boolean townUpgrade) {
        this.townUpgrade = townUpgrade;
    }

    public void upgradeResourceStorage() {
        if (resourceStorageUpgrade < 2) {
            resourceStorageUpgrade++;
        }
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
    }

    public void addIronToStorage(int amount) {
        resourceStorage.put(Resource.IRON, resourceStorage.get(Resource.IRON) + amount);
    }

    public void addFoodToStorage(int amount) {
        resourceStorage.put(Resource.FOOD, resourceStorage.get(Resource.FOOD) + amount);
    }

    public void addWoodToStorage(int amount) {
        resourceStorage.put(Resource.WOOD, resourceStorage.get(Resource.WOOD) + amount);
    }

    // Safeguard resource generator

    public void safeGuardGenerator() {
        addStoneToStorage(SAFE_GUARD_VALUE);
        addIronToStorage (SAFE_GUARD_VALUE);
        addFoodToStorage (SAFE_GUARD_VALUE);
        addWoodToStorage (SAFE_GUARD_VALUE);
    }

    // --- unit list ---

    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }

    public boolean addUnit(Unit unit) {
        Objects.requireNonNull(unit, "worker");
        if (!isActive() || units.contains(unit)) {
            return false;
        }
        return units.add(unit);
    }

    public boolean removeUnit(Unit unit) {
        return units.remove(unit);
    }

    public boolean generateUnit(UnitType unitType) {

        if (unitNumber >= unitCap) return false;

        if (unitType == UnitType.WORKER) {
            units.add(new Worker(getOwner(), getPosition()));
        } else if (unitType == UnitType.BUILDER) {
            units.add(new Builder(getOwner(), getPosition()));
        } else if (unitType == UnitType.EXPLORER) {
            units.add(new Explorer(getOwner(), getPosition()));
        } else if (unitType == UnitType.BORDER_EXPANDER) {
            units.add(new BorderExpander(getOwner(), getPosition()));
        } else {
            throw new IllegalArgumentException("undefined type : " + unitType);
        }

        unitNumber++;
        return true;
    }

    @Override
    public void registerUnpaidUpkeep() {}
}
