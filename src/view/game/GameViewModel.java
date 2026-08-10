package view.game;

import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.unit.Unit;

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
}
