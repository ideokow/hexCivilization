package model.game.building;

import model.game.happiness.Era;
import model.game.hex.*;
import model.game.player.Player;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;
import model.game.unit.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
production buildings model
buildings: Lumber Mill, Stone Mine, Iron Mine, Farm, Stable
 */
public class ProductionBuilding extends Building {

    private final ProductionType productionType;
    private final List<Worker> stationedWorkers;

    public ProductionBuilding(boolean ownedByPlayer, ProductionType type, HexCoordinate position) {
        super(Objects.requireNonNull(type, "type").getBuildingType(), ownedByPlayer, position);
        this.productionType = type;
        this.stationedWorkers = new ArrayList<>();
    }

    public ProductionType getProductionBuildingType() {
        return productionType;
    }

    public ProductionType getProductionType() {
        return productionType;
    }

    public List<Worker> getUnits() {
        return new ArrayList<>(stationedWorkers);
    }

    public List<Worker> getStationedWorkers() {
        return new ArrayList<>(stationedWorkers);
    }

    public StationResult stationWorker(Worker worker) {

        // --- check requirement ---

        // capacity check
        if (stationedWorkers.size() >= getProductionType().getWorkerCapacity()) {
            return StationResult.CAPACITY_REACHED;
        }
        // entry ap check
        if (productionType.getStationApCost() > worker.getCurrentAP()) {
            return StationResult.NOT_ENOUGH_STATION_AP;
        }
        // is worker in building check
        Objects.requireNonNull(worker, "worker");
        if (isRuined() || stationedWorkers.contains(worker)) {
            return StationResult.WORKER_IS_IN_ALREADY;
        }
        // worker is in hex check
        if (worker.getPosition() == null
                || !worker.getPosition().equals(getPosition())) {
            return StationResult.WORKER_IS_NOT_HERE;
        }
        // building is ok
        if (isRuined()) {
            return StationResult.BUILDING_IS_RUINED;
        }
        // worker is not owned
        if (!worker.isOwnedByPlayer()) {
            return StationResult.ANOTHER_PLAYER_WORKER;
        }

        stationedWorkers.add(worker);
        worker.setInBuilding(true);
        worker.spendAP(productionType.getStationApCost());
        return StationResult.SUCCESS;
    }

    public boolean removeWorker(Worker worker) {
        if (stationedWorkers.contains(worker)) {
            stationedWorkers.remove(worker);
            worker.setInBuilding(false);
            worker.addAP(productionType.getStationApCost());
            return true;
        } else {
            return false;
        }
    }

    public int getProductionAmount(TownHall townHall) {
        int baseRate = productionType.getProductionRate();
        int workerN = stationedWorkers.size();

        // --- tech & happiness coefficient ---
        double coefficient = 1.0;
        if (productionType.equals(ProductionType.IRON_MINE) || productionType.equals(ProductionType.STONE_MINE)) {
            if (townHall.getTechnologies().isAcquired(Technology.GOOD_TOOLS)) coefficient *= 1.5;
            if (townHall.getTechnologies().isAcquired(Technology.METALWORKING_TOOLS)) coefficient *= 1.5;
        }
        if (townHall.getHappiness().getEra().equals(Era.GOLDEN_ERA)) coefficient *= 1.1;
        else if (townHall.getHappiness().getEra().equals(Era.DISCONTENT_ERA)) coefficient *= 0.9;

        // --- adjacency rules ---
        int adjacencyBonus = 0;
        // Farm (+2 for each border)
        if (productionType.equals(ProductionType.FARM)) {
            adjacencyBonus = neighborFarms(townHall.getGrid(), getPosition());
        }
        // Lumber mill in beach bonus
        else if (productionType.equals(ProductionType.LUMBER_MILL)) {
            if (neighborHexes(townHall.getGrid(), getPosition(), TerrainType.SEA) > 0) {
                adjacencyBonus = 2;
            }
        }
        // Efficient Mines (in mountains ones)
        else if (
            productionType.equals(ProductionType.IRON_MINE) ||
            productionType.equals(ProductionType.STONE_MINE)
        ) {
            if (
                neighborHexes(townHall.getGrid(), getPosition(), TerrainType.MOUNTAIN) +
                neighborHexes(townHall.getGrid(), getPosition(), TerrainType.MOUNTAIN_RANGE) >= 2) {
                adjacencyBonus = 1;
            }
        }

        return ((int)(((double) (baseRate * workerN)) * coefficient)) + adjacencyBonus;
    }

    private static int neighborFarms(HexGrid hexGrid, HexCoordinate position) {
        int c = 0;
        for (Hex hex : hexGrid.hexesInRange(position, 1)) {
            if (hex.getBuilding() != null && hex.getBuilding().getType().equals(BuildingType.FARM)) {
                c += 1;
            }
        }
        return c;
    }

    private static int neighborHexes(HexGrid hexGrid, HexCoordinate position, TerrainType terrain) {
        int c = 0;
        for (Hex hex : hexGrid.hexesInRange(position, 1)) {
            if (hex.getTerrain().equals(terrain)) {
                c += 1;
            }
        }
        return c;
    }
}
