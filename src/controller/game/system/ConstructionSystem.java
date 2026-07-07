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

    public BuildResult canBuild(Player player, Unit unit, BuildingType type, HexCoordinate coordinate) {
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

        return BuildResult.SUCCESS;
    }

    private boolean upgradeCheck(BuildingType buildingType) {
        if (buildingType == BuildingType.STONE_MINE && !townHall.isStoneUpgrade()) {
            return false;
        }
        if (buildingType == BuildingType.IRON_MINE && !townHall.isIronUpgrade()) {
            return false;
        }
        if (buildingType == BuildingType.TOWN && !townHall.isTownUpgrade()) {
            return false;
        }
        if (buildingType == BuildingType.VILLAGE && !townHall.isTownUpgrade()) {
            return false;
        }
        return true;
    }

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
        return BuildResult.SUCCESS;
    }

    private BuildResult checkPlacement(Hex hex, BuildingType type) {
        TerrainType requiredTerrain = getRequiredTerrain(type);
        if (requiredTerrain != null && hex.getTerrain() != requiredTerrain) {
            return BuildResult.WRONG_TERRAIN;
        }

        Resource requiredResource = getRequiredResource(type);
        if (requiredResource != null && !hex.isAvailable(requiredResource)) {
            return BuildResult.MISSING_HEX_RESOURCE;
        }

        if (PopulationType.fromBuildingType(type) != null && !hex.getAvailableResources().isEmpty()) {
            return BuildResult.HEX_HAS_RESOURCE;
        }

        return BuildResult.SUCCESS;
    }

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

        return 0;
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
}
