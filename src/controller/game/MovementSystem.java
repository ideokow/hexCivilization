package controller.game;

import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.unit.Unit;
import model.game.unit.Worker;

import java.util.Objects;

public class MovementSystem {

    private final HexGrid grid;

    public MovementSystem(HexGrid grid) {
        this.grid = Objects.requireNonNull(grid, "grid");
    }

    public MoveResult canMove(Unit unit, HexCoordinate targetCoordinate) {
        if (unit == null || targetCoordinate == null) {
            return MoveResult.UNIT_NOT_ON_MAP;
        }

        Hex from = grid.get(unit.getPosition());
        Hex to = grid.get(targetCoordinate);

        if (from == null || to == null || !grid.contains(from.getCoordinate()) || !from.getUnits().contains(unit)) {
            return MoveResult.UNIT_NOT_ON_MAP;
        }
        if (!from.getCoordinate().isNeighbor(targetCoordinate)) {
            return MoveResult.NOT_NEIGHBOR;
        }
        if (!grid.isDiscovered(targetCoordinate)) {
            return MoveResult.HEX_NOT_DISCOVERED;
        }
        if (unit.getCurrentAP() < to.getTerrain().getMovementCost()) {
            return MoveResult.NOT_ENOUGH_AP;
        }
        if (unit instanceof Worker && ((Worker) unit).isInBuilding()) {
            return MoveResult.UNIT_IS_IN_BUILDING;
        }

        return MoveResult.SUCCESS;
    }

    public MoveResult move(Unit unit, HexCoordinate targetCoordinate) {
        MoveResult result = canMove(unit, targetCoordinate);
        if (result != MoveResult.SUCCESS) {
            return result;
        }

        Hex from = grid.get(unit.getPosition());
        Hex to = grid.get(targetCoordinate);

        unit.spendAP(to.getTerrain().getMovementCost());
        from.removeUnit(unit);
        to.addUnit(unit);
        unit.setPosition(to.getCoordinate());
        grid.discoverAround(to.getCoordinate(), unit.getType().getVisibilityRadius());

        return MoveResult.SUCCESS;
    }
}
