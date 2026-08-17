package model.game.townhall;

import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.PopulationType;
import model.game.happiness.Happiness;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.registry.MilitaryRegistry;
import model.game.registry.UnitRegistry;
import model.game.townhall.opration.GenerationStatus;
import model.game.unit.*;
import model.game.unit.military.Archer;
import model.game.unit.military.Swordsman;

import java.util.*;

public class TownHall extends Building {

    public static final int SAFE_GUARD_VALUE = 1;

    private final HexGrid grid;

    private final Map<Resource, Integer> resourceStorage;

    private final Technologies technologies;
    private Level level;

    private final Happiness happiness = new Happiness();

    private int unitCap;
    private static final int BASE_MILITARY_UNIT_CAP = 10;
    private static final int BASE_UNIT_CAP = 10;
    private int unitNumber = 0;

    private boolean dockBuildingBonus = false;

    public TownHall(HexGrid grid) {

        super(BuildingType.TOWN_HALL, true, new HexCoordinate(0, 0));
        this.grid = grid;

        resourceStorage = new HashMap<>();
        resourceStorage.put(Resource.STONE, 0);
        resourceStorage.put(Resource.IRON,  0);
        resourceStorage.put(Resource.FOOD, 10);
        resourceStorage.put(Resource.WOOD, 10);

        unitCap = BASE_UNIT_CAP;
        level = Level.LEVEL_1;
        technologies = new Technologies(this);

        generateInitialUnits();
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

    public Happiness getHappiness() {
        return happiness;
    }

    public HexGrid getGrid() {
        return grid;
    }

    public boolean hasDockBuildingBonus() {
        return dockBuildingBonus;
    }

    public void giveDockBuildingBonus() {
        dockBuildingBonus = true;
    }

    public void useDockBuildingBonus() {
        dockBuildingBonus = false;
    }

    // --- Level Getter/Setter ---

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
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

    public int getResourceAmount(Resource resource) {
        if (resource == null) return 0;
        return resourceStorage.getOrDefault(resource, 0);
    }

    public int getAvailableStorage(Resource resource) {
        if (resource == null) return 0;
        return Math.max(0, getResourceCap() - getResourceAmount(resource));
    }

    public int addResource(Resource resource, int amount) {
        if (resource == null || amount <= 0) return 0;

        int addedAmount = Math.min(amount, getAvailableStorage(resource));
        resourceStorage.put(resource, getResourceAmount(resource) + addedAmount);
        return addedAmount;
    }

    public boolean deductResource(Resource resource, int amount) {
        if (resource == null || amount <= 0 || getResourceAmount(resource) < amount) {
            return false;
        }

        resourceStorage.put(resource, getResourceAmount(resource) - amount);
        return true;
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

    public void spendResources(Map<Resource, Integer> cost) {
        if (!canAfford(cost)) {
            return;
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
    }

    public void addResources(Map<Resource, Integer> resources) {
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
    }

    // --- unit cap ---

    public int getUnitCap() {
        return unitCap;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setUnitCap(int townsNumber, int villageNumber) {
        unitCap = (int) (((double) BASE_UNIT_CAP) *
                (((double) townsNumber * PopulationType.TOWN.getUnitCapIncrease()) +
                ((double) villageNumber * PopulationType.VILLAGE.getUnitCapIncrease()) +
                1.0)
        );
        if (unitCap < unitNumber) unitCap = unitNumber;
    }

    public int getMilitaryUnitCap() {
        return (BASE_MILITARY_UNIT_CAP * level.getLevelN()) + ((int) (0.2 * getUnitCap()));
    }

    public void increaseUnitNumber() {
        unitNumber++;
    }

    public void decreaseUnitNumber() {
        unitNumber--;
    }

    // --- unit generation ---

    private void generateInitialUnits() {
        generateUnit(UnitType.BUILDER);
        generateUnit(UnitType.BUILDER);
        generateUnit(UnitType.WORKER);
        generateUnit(UnitType.WORKER);
        generateUnit(UnitType.EXPLORER);
    }

    public void generateUnit(UnitType unitType) {

        if (!canGenerateUnit(unitType).equals(GenerationStatus.SUCCESS)) return;

        // make unit
        Unit unit;
        if (unitType == UnitType.WORKER) {
            unit = new Worker(getPosition());
        }
        else if (unitType == UnitType.BUILDER) {
            unit = new Builder(getPosition());
        }
        else if (unitType == UnitType.EXPLORER) {
            unit = new Explorer(getPosition());
        }
        else if (unitType == UnitType.BORDER_EXPANDER) {
            unit = new BorderExpander(getPosition());
        }
        else if (unitType == UnitType.SWORDSMAN) {
            unit = new Swordsman(getPosition());
        }
        else if (unitType == UnitType.ARCHER) {
            unit = new Archer(getPosition());
        }
        else {
            throw new IllegalArgumentException("Undefined type : " + unitType);
        }

        // full ap
        unit.resetAP(happiness.getEra());

        // add to registry
        UnitRegistry.getInstance().addUnit(unit);

        // military cap reach impact on public contest
        if (MilitaryRegistry.getInstance().getMilitaryUnitsNumber() == getMilitaryUnitCap()) {
            happiness.addHappiness(-1);
        }

        // place unit on hex
        grid.get(new HexCoordinate(0, 0)).addUnit(unit);
        unitNumber++;
    }

    public GenerationStatus canGenerateUnit(UnitType unitType) {
        if (unitNumber >= unitCap)
            return GenerationStatus.UNIT_CAP_REACHED;
        if (!canAfford(unitType.getCost()))
            return GenerationStatus.CANT_AFFORD_COST;
        if (getLevel().getLevelN() < unitType.getMinimumLevel().getLevelN())
            return GenerationStatus.NOT_ENOUGH_LEVEL;
        if (unitType.equals(UnitType.CAVALRY))
            return GenerationStatus.CANT_BUILD_CAVALRY;
        if (MilitaryRegistry.getInstance().getMilitaryUnitsNumber() >= getMilitaryUnitCap()) {
            return GenerationStatus.MILITARY_UNIT_CAP_REACHED;
        }
        return GenerationStatus.SUCCESS;
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        return true;
    }
}
