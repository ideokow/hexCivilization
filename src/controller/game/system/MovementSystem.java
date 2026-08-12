package controller.game.system;

import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.TerrainType;
import model.game.season.SeasonName;
import model.game.unit.Explorer;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.Worker;

import java.util.Objects;

public class MovementSystem {

    private final HexGrid grid;

    public MovementSystem(HexGrid grid) {
        this.grid = Objects.requireNonNull(grid, "grid");
    }

    /*
    cost of entering a hex: terrain cost, reduced on roads,
    increased by winter (+1 on every hex) and fall (+1 on sea hexes)
     */
    public static int enterCost(Hex hex, SeasonName season) {
        int cost = hex.getTerrain().getMovementCost();

        if (hex.isThereRoad()) {
            cost = Math.max(1, cost / 2);
        }
        if (season == SeasonName.WINTER) {
            cost += 1;
        }
        if (season == SeasonName.FALL && hex.getTerrain() == TerrainType.SEA) {
            cost += 1;
        }
        return cost;
    }

    // mountain ranges can never be crossed; seas need sailing technology
    public static boolean canEnter(Hex hex, boolean canSail) {
        TerrainType terrain = hex.getTerrain();
        if (terrain == TerrainType.MOUNTAIN_RANGE) {
            return false;
        }
        return terrain != TerrainType.SEA || canSail;
    }

    public MoveResult canMove(Unit unit, HexCoordinate targetCoordinate) {
        return canMove(unit, targetCoordinate, null, false);
    }

    public MoveResult canMove(Unit unit, HexCoordinate targetCoordinate, SeasonName season, boolean canSail) {
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
        if (!canEnter(to, canSail)) {
            return MoveResult.TERRAIN_IMPASSABLE;
        }
        if (unit.getCurrentAP() < enterCost(to, season)) {
            return MoveResult.NOT_ENOUGH_AP;
        }
        if (unit instanceof Worker && ((Worker) unit).isInBuilding()) {
            return MoveResult.UNIT_IS_IN_BUILDING;
        }

        return MoveResult.SUCCESS;
    }

    public MoveResult move(Unit unit, HexCoordinate targetCoordinate) {
        return move(unit, targetCoordinate, null, false);
    }

    public MoveResult move(Unit unit, HexCoordinate targetCoordinate, SeasonName season, boolean canSail) {
        MoveResult result = canMove(unit, targetCoordinate, season, canSail);
        if (result != MoveResult.SUCCESS) {
            return result;
        }

        Hex from = grid.get(unit.getPosition());
        Hex to = grid.get(targetCoordinate);

        unit.spendAP(enterCost(to, season));
        from.removeUnit(unit);
        to.addUnit(unit);
        unit.setPosition(to.getCoordinate());
        grid.discoverAround(to.getCoordinate(), unit.getType().getVisibilityRadius());

        // explorer trigger
        if (unit.getType() == UnitType.EXPLORER) {
            ((Explorer) unit).exploreMap(grid);
        }

        return MoveResult.SUCCESS;
    }
}
