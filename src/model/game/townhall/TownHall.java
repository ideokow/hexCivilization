package model.game.townhall;

import model.game.building.Building;
import model.game.building.BuildingType;
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

    private final Technologies technologies;
    private final Level level;

    private int unitCap;
    private final int baseUnitCap = 10;
    private int unitNumber = 0;

    public TownHall(HexGrid grid, Player owner) {

        super(BuildingType.TOWN_HALL, owner, new HexCoordinate(0, 0));
        this.grid = grid;

        resourceStorage = new HashMap<>();
        resourceStorage.put(Resource.STONE, 0);
        resourceStorage.put(Resource.IRON,  0);
        resourceStorage.put(Resource.FOOD, 10);
        resourceStorage.put(Resource.WOOD, 10);

        unitCap = baseUnitCap;

        level = Level.LEVEL_1;

        technologies = new Technologies(this);
    }

    // --- Getters ---

    public int getHP(){
        return level.getBaseMaximumHP() + (technologies.isAcquired(Technology.DEFENCE) ? 50 : 0);
    }

    public Technologies getTechnologies() {
        return technologies;
    }

    public int getResourceCap() {
        return level.getResourceCap();
    }

    // --- unit cap ---

    public int getUnitCap() {
        return unitCap;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setUnitCap(int townsNumber, int villageNumber) {
        unitCap = (int) (((double) baseUnitCap) * (((double) townsNumber * 1) + ((double) villageNumber * 0.5) + 1.0));
        if (unitCap < unitNumber) unitCap = unitNumber;
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
        if (resourceStorage.get(Resource.STONE) > level.getResourceCap()) {
            resourceStorage.put(Resource.STONE, level.getResourceCap());
        }
    }

    public void addIronToStorage(int amount) {
        resourceStorage.put(Resource.IRON, resourceStorage.get(Resource.IRON) + amount);
        if (resourceStorage.get(Resource.IRON) > level.getResourceCap()) {
            resourceStorage.put(Resource.IRON, level.getResourceCap());
        }
    }

    public void addFoodToStorage(int amount) {
        resourceStorage.put(Resource.FOOD, resourceStorage.get(Resource.FOOD) + amount);
        if (resourceStorage.get(Resource.FOOD) > level.getResourceCap()) {
            resourceStorage.put(Resource.FOOD, level.getResourceCap());
        }
    }

    public void addWoodToStorage(int amount) {
        resourceStorage.put(Resource.WOOD, resourceStorage.get(Resource.WOOD) + amount);
        if (resourceStorage.get(Resource.WOOD) > level.getResourceCap()) {
            resourceStorage.put(Resource.WOOD, level.getResourceCap());
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
        addWoodToStorage(SAFE_GUARD_VALUE);
        addFoodToStorage(SAFE_GUARD_VALUE);
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

    public boolean addResources(Map<Resource, Integer> resources) {
        if (resources.containsKey(Resource.STONE)) {
            addStoneToStorage(resources.get(Resource.STONE));
        }
        if (resources.containsKey(Resource.IRON)) {
            addIronToStorage(resources.get(Resource.IRON));
        }
        if (resources.containsKey(Resource.FOOD)) {
            addFoodToStorage(resources.get(Resource.FOOD));
        }
        if (resources.containsKey(Resource.WOOD)) {
            addWoodToStorage(resources.get(Resource.WOOD));
        }
        return true;
    }

    // --- unit generation ---

    public boolean generateUnit(UnitType unitType) {

        if (unitNumber >= unitCap) {
            return false;
        }

        Unit unit;

        // make unit
        if (unitType == UnitType.WORKER) {
            unit = new Worker(getOwner(), getPosition());
        }
        else if (unitType == UnitType.BUILDER) {
            unit = new Builder(getOwner(), getPosition());
        }
        else if (unitType == UnitType.EXPLORER) {
            unit = new Explorer(getOwner(), getPosition());
        }
        else if (unitType == UnitType.BORDER_EXPANDER) {
            unit = new BorderExpander(getOwner(), getPosition());
        }
        else {
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
