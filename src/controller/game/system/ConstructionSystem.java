package controller.game.system;

import model.game.building.*;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.hex.TerrainType;
import model.game.player.Player;
import model.game.registry.BuildingRegistry;
import model.game.registry.UnitRegistry;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;
import model.game.unit.Builder;
import model.game.unit.Unit;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class ConstructionSystem {

    private final HexGrid grid;
    private final TownHall townHall;

    public ConstructionSystem(HexGrid grid, TownHall townHall) {
        this.grid = Objects.requireNonNull(grid, "grid is null!");
        this.townHall = Objects.requireNonNull(townHall, "town hall is null");
    }

    // --- functions for building things ---

    /*
    check building possibility
     */
    private BuildResult canBuild(Player player, Unit unit, BuildingType type, HexCoordinate coordinate) {
        if (player == null || type == null || coordinate == null) {
            return BuildResult.UNIT_NOT_ON_MAP;
        }
        if (type == BuildingType.TOWN_HALL) {
            return BuildResult.CANT_BUILD_TOWN_HALL;
        }
        if (!(unit instanceof Builder)) {
            return BuildResult.NOT_A_BUILDER;
        }

        // upgrade tech
        if (!upgradeCheck(type)) {
            return BuildResult.UPGRADE_REQUIRED;
        }

        Hex hex = grid.get(coordinate);
        if (unit.getPosition() == null || !unit.getPosition().equals(hex.getCoordinate())) {
            return BuildResult.BUILDER_NOT_ON_HEX;
        }
        if (!grid.isDiscovered(coordinate)) {
            return BuildResult.HEX_NOT_DISCOVERED;
        }
        if (!player.ownsTerritory(coordinate)) {
            return BuildResult.OUTSIDE_TERRITORY;
        }
        if (hex.getBuilding() != null) {
            return BuildResult.HEX_HAS_BUILDING;
        }

        BuildResult placementResult = checkPlacement(hex, type);
        if (placementResult != BuildResult.SUCCESS) {
            return placementResult;
        }
        if (!townHall.canAfford(getConstructionCost(type))) {
            return BuildResult.NOT_ENOUGH_RESOURCES;
        }
        if (unit.getCurrentAP() < getBuildApCost(type)) {
            return BuildResult.NOT_ENOUGH_AP;
        }
        if (!((Builder) unit).hasCharges()) {
            return BuildResult.NOT_ENOUGH_CHARGE;
        }

        // check specifically docks
        if (type.equals(BuildingType.DOCK) && !Dock.canBuildDock(grid, coordinate)) {
            return BuildResult.NO_WATER_FOR_DOCK;
        }

        return BuildResult.SUCCESS;
    }

    /*
    check user's technology is ok for a specific building type
     */
    private boolean upgradeCheck(BuildingType buildingType) {
        if (buildingType == BuildingType.STONE_MINE && !townHall.getTechnologies().isAcquired(Technology.STONE)) {
            return false;
        }
        if (buildingType == BuildingType.IRON_MINE && !townHall.getTechnologies().isAcquired(Technology.IRON)) {
            return false;
        }
        if (buildingType == BuildingType.TOWN && !townHall.getTechnologies().isAcquired(Technology.URBANIZATION)) {
            return false;
        }
        if (buildingType == BuildingType.VILLAGE && !townHall.getTechnologies().isAcquired(Technology.URBANIZATION)) {
            return false;
        }
        return true;
    }

    /*
    build buildings!
     */
    public BuildResult build(Player player, Unit unit, BuildingType type, HexCoordinate coordinate) {
        BuildResult result = canBuild(player, unit, type, coordinate);
        if (result != BuildResult.SUCCESS) {
            return result;
        }

        Builder builder = (Builder) unit;
        Hex hex = grid.get(coordinate);

        Building building = createBuilding(player, type, coordinate);

        townHall.spendResources(getConstructionCost(type));
        builder.spendAP(getBuildApCost(type));
        hex.setBuilding(building);
        builder.consumeCharge();

        if (!builder.hasCharges()) {
            UnitRegistry.getInstance().removeUnit(builder);
            hex.removeUnit(builder);
            townHall.decreaseUnitNumber();
        }

        BuildingRegistry.getInstance().addBuilding(building);
        if (type == BuildingType.VILLAGE || type == BuildingType.TOWN) {
            BuildingRegistry.getInstance().refreshUnitCap(townHall);
        }

        townHall.getHappiness().addHappiness(-1);
        return BuildResult.SUCCESS;
    }

    /*
    check there's space for building
     */
    private BuildResult checkPlacement(Hex hex, BuildingType type) {
        // check hex requirements
        TerrainType requiredTerrain = getRequiredTerrain(type);
        if (requiredTerrain != null && hex.getTerrain() != requiredTerrain) {
            return BuildResult.WRONG_TERRAIN;
        }
        Resource requiredResource = getRequiredResource(type);
        if (requiredResource != null && !hex.isAvailable(requiredResource)) {
            return BuildResult.MISSING_HEX_RESOURCE;
        }

        if (type == BuildingType.FARM && !hex.isAvailable(Resource.FOOD)) {
            return BuildResult.MISSING_HEX_RESOURCE;
        }
        if (type == BuildingType.STABLE && !hex.isAvailable(Resource.FOOD)) {
            return BuildResult.MISSING_HEX_RESOURCE;
        }
        if (type == BuildingType.IRON_MINE && !hex.isAvailable(Resource.IRON)) {
            return BuildResult.MISSING_HEX_RESOURCE;
        }

        if (PopulationType.fromBuildingType(type) != null && !hex.getAvailableResources().isEmpty()) {
            return BuildResult.HEX_HAS_RESOURCE;
        }

        return BuildResult.SUCCESS;
    }

    /*
    other useful things for building
     */
    private TerrainType getRequiredTerrain(BuildingType type) {
        ProductionType productionType = ProductionType.fromBuildingType(type);
        if (productionType == null) {
            return null;
        }
        return productionType.getRequiredTerrain();
    }

    private Resource getRequiredResource(BuildingType type) {
        ProductionType productionType = ProductionType.fromBuildingType(type);
        if (productionType == null) {
            return null;
        }
        return productionType.getRequiredResource();
    }

    private int getBuildApCost(BuildingType type) {
        ProductionType productionType = ProductionType.fromBuildingType(type);
        if (productionType != null) {
            return productionType.getBuildApCost();
        }

        PopulationType populationType = PopulationType.fromBuildingType(type);
        if (populationType != null) {
            return populationType.getBuildApCost();
        }

        // for rest of types!
        return 1;
    }

    private Map<Resource, Integer> getConstructionCost(BuildingType type) {
        ProductionType productionType = ProductionType.fromBuildingType(type);
        if (productionType != null) {
            return productionType.getConstructionCost();
        }

        PopulationType populationType = PopulationType.fromBuildingType(type);
        if (populationType != null) {
            return populationType.getConstructionCost();
        }

        return new EnumMap<>(Resource.class);
    }

    private Building createBuilding(Player owner, BuildingType type, HexCoordinate coordinate) {
        return switch (type) {
            case LUMBER_MILL -> new ProductionBuilding(owner, ProductionType.LUMBER_MILL, coordinate);
            case STONE_MINE -> new ProductionBuilding(owner, ProductionType.STONE_MINE, coordinate);
            case IRON_MINE -> new ProductionBuilding(owner, ProductionType.IRON_MINE, coordinate);
            case FARM -> new ProductionBuilding(owner, ProductionType.FARM, coordinate);
            case STABLE -> new ProductionBuilding(owner, ProductionType.STABLE, coordinate);
            case VILLAGE, TOWN -> new PopulationBuilding(owner, type, coordinate);
            default -> throw new IllegalArgumentException("Unsupported building type: " + type);
        };
    }

    // --- functions for ruining ---

    private RuinStatus canRuin(Unit unit, Building building) {
        if (unit == null || building == null) {
            return RuinStatus.NULL_ERR;
        }
        if (building.isRuined()) {
            return RuinStatus.BUILDING_RUINED_ALREADY;
        }
        if (building.getType().equals(BuildingType.TOWN_HALL)) {
            return RuinStatus.CANT_RUIN_TOWN_HALL;
        }
        if (!unit.getPosition().equals(building.getPosition())) {
            return RuinStatus.BUILDER_IS_NOT_HERE;
        }
        if (!(unit instanceof Builder)) {
            return RuinStatus.NOT_A_BUILDER;
        }
        if (unit.getCurrentAP() < getBuildApCost(building.getType())) {
            return RuinStatus.LOW_AP;
        }
        if (!((Builder) unit).hasCharges()) {
            return RuinStatus.NOT_ENOUGH_CHARGE;
        }

        return RuinStatus.SUCCESS;
    }

    public RuinStatus ruin(Unit unit, Building building) {
        RuinStatus ruinStatus = canRuin(unit, building);
        if (!ruinStatus.equals(RuinStatus.SUCCESS)) return ruinStatus;

        Builder builder = (Builder) unit;
        builder.spendAP(getBuildApCost(building.getType()));
        builder.consumeCharge();

        if (!builder.hasCharges()) {
            UnitRegistry.getInstance().removeUnit(builder);
            grid.get(building.getPosition()).removeUnit(builder);
            townHall.decreaseUnitNumber();
        }

        BuildingRegistry.getInstance().ruinBuilding(grid, building);
        if (building.getType() == BuildingType.VILLAGE || building.getType() == BuildingType.TOWN) {
            BuildingRegistry.getInstance().refreshUnitCap(townHall);
        }

        return RuinStatus.SUCCESS;
    }
}