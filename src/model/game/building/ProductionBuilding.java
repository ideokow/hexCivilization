package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.player.Player;
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

        if (stationedWorkers.size() >= getProductionType().getWorkerCapacity()) {
            return StationResult.CAPACITY_REACHED;
        }

        Objects.requireNonNull(worker, "worker");
        if (isRuined() || stationedWorkers.contains(worker)) {
            return StationResult.WORKER_IS_IN_ALREADY;
        }
        stationedWorkers.add(worker);
        worker.setInBuilding(true);
        return StationResult.SUCCESS;
    }

    public boolean removeWorker(Worker worker) {
        if (stationedWorkers.contains(worker)) {
            stationedWorkers.remove(worker);
            worker.setInBuilding(false);
            return true;
        } else {
            return false;
        }
    }

    public int getProductionAmount(TownHall townHall) {
        int baseRate = productionType.getProductionRate();
        int workerN = stationedWorkers.size();
        double techCoefficient = townHall.getUpgrades().isToolsUpgrade() ? 1.5 : 1.0;
        return ((int)(((double) (baseRate * workerN)) * techCoefficient));
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
