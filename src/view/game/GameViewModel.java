package view.game;

import model.game.building.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.building.Upgrade;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import java.util.Collection;
import java.util.List;

public interface GameViewModel {

    int getTurnNumber();

    TownHall getTownHall();

    Collection<Unit> getUnits();

    boolean containsUnit(Unit unit);

    Hex getHex(HexCoordinate coordinate);

    boolean containsHex(HexCoordinate coordinate);

    boolean isDiscovered(HexCoordinate coordinate);

    boolean ownsTerritory(HexCoordinate coordinate);

    List<Hex> getHexesInBounds(
            int minQ,
            int maxQ,
            int minR,
            int maxR
    );

    int getNetResource(Resource resource);

    Upgrade getInQueueUpgrade();

    int getUpgradeRemainingTurns();

    UnitType getInQueueUnitType();

    int getGenerationRemainingTurns();
}
