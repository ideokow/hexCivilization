package model.game.building;

import model.game.happiness.Era;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.TerrainType;
import model.game.player.Player;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;

import java.util.List;

/*
production building model for Dock (specially)
 */
public class Dock extends ProductionBuilding {

    private static final int RADIUS = 3;

    public Dock(Player owner, HexCoordinate position) {
        super(owner, ProductionType.DOCK, position);
    }

    @Override
    public int getProductionAmount(TownHall townHall) {
        // ordinary production amount parameters
        int baseRate = getProductionType().getProductionRate();
        int workerN = getStationedWorkers().size();

        // sea hexes
        int seaHexesN = countSeaHexes(townHall.getGrid().hexesInRange(getPosition(), RADIUS));

        // happiness & tech multiplier
        double coefficient = 1.0;
        if (
            getProductionType().equals(ProductionType.IRON_MINE) ||
            getProductionType().equals(ProductionType.STONE_MINE)) {
            if (townHall.getTechnologies().isAcquired(Technology.GOOD_TOOLS)) coefficient *= 1.5;
            if (townHall.getTechnologies().isAcquired(Technology.METALWORKING_TOOLS)) coefficient *= 1.5;
        }
        if (townHall.getHappiness().getEra().equals(Era.GOLDEN_ERA)) coefficient *= 1.1;
        else if (townHall.getHappiness().getEra().equals(Era.DISCONTENT_ERA)) coefficient *= 0.9;

        return ((int)(((double) (baseRate * workerN * seaHexesN)) * coefficient));
    }

    private static int countSeaHexes(List<Hex> hexesInRange) {
        int c = 0;
        for (Hex hex: hexesInRange) {
            if (hex.getTerrain().equals(TerrainType.SEA)) c += 1;
        }
        return c;
    }

    public static boolean canBuildDock(HexGrid grid, HexCoordinate position) {
        return countSeaHexes(grid.hexesInRange(position, 1)) != 0;
    }
}
