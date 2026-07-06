package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.unit.Unit;
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

    public List<Unit> getUnits() {
        return new ArrayList<>(stationedWorkers);
    }

    public List<Worker> getStationedWorkers() {
        return new ArrayList<>(stationedWorkers);
    }

    public boolean stationWorker(Worker worker) {
        Objects.requireNonNull(worker, "worker");
        if (!isActive() || stationedWorkers.contains(worker)) {
            return false;
        }
        return stationedWorkers.add(worker);
    }

    public boolean removeWorker(Worker worker) {
        return stationedWorkers.remove(worker);
    }
}
