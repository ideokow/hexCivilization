package model.game.building;

import model.game.happiness.Era;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;
import model.game.unit.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
production buildings model
buildings: Lumber Mill, Stone Mine, Iron Mine, Farm, Stable
 */
public class ProductionBuilding extends Building {

    private final ProductionType productionType;
    private final List<Worker> stationedWorkers;

    public ProductionBuilding(Player owner, ProductionType type, HexCoordinate position) {
        super(Objects.requireNonNull(type, "type").getBuildingType(), owner, position);
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
        if (!getOwner().equals(worker.getOwner())) {
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

        double coefficient = 1.0;

        if (productionType.equals(ProductionType.IRON_MINE) || productionType.equals(ProductionType.STONE_MINE)) {
            if (townHall.getTechnologies().isAcquired(Technology.GOOD_TOOLS)) coefficient *= 1.5;
            if (townHall.getTechnologies().isAcquired(Technology.METALWORKING_TOOLS)) coefficient *= 1.5;
        }

        if (townHall.getHappiness().getEra().equals(Era.GOLDEN_ERA)) coefficient *= 1.1;
        else if (townHall.getHappiness().getEra().equals(Era.DISCONTENT_ERA)) coefficient *= 0.9;

        return ((int)(((double) (baseRate * workerN)) * coefficient));
    }

    @Override
    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        /*
        One unit of each material required for maintenance cost
         */
        Map<Resource, Integer> cost = productionType.getConstructionCost();
        for (Resource resource : cost.keySet()) {
            cost.put(resource, 1);
        }

        if (townHall.canAfford(cost)) {
            townHall.spendResources(cost);
            resetUnpaidUpkeepTurns();
            return true;
        } else {
            increaseUnpaidUpkeepTurns();
            return false;
        }
    }
}
