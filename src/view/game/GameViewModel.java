package view.game;

import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.Wall;
import model.game.season.SeasonName;
import model.game.townhall.Technology;
import model.game.townhall.operation.TownHallOperation;
import model.game.tribe.Tribe;
import model.game.unit.Unit;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface GameViewModel {

    int getTurnNumber();

    TownHall getTownHall();

    Collection<Unit> getUnits();

    boolean containsUnit(Unit unit);

    Hex getHex(HexCoordinate coordinate);

    List<Wall> getWalls(HexCoordinate coordinate);

    Set<Wall> getAllWalls();

    Tribe getTribe(HexCoordinate coordinate);

    boolean containsHex(HexCoordinate coordinate);

    boolean isDiscovered(HexCoordinate coordinate);

    boolean ownsTerritory(HexCoordinate coordinate);

    boolean hasTradedAtBazaarThisTurn();

    boolean hasTradedAtTradingPostThisTurn();

    boolean hasTradedWithTribeThisTurn(Tribe tribe);

    List<Hex> getHexesInBounds(
            int minQ,
            int maxQ,
            int minR,
            int maxR
    );

    int getNetResource(Resource resource);

    TownHallOperation getInQueueOperation();

    List<Technology> getAcquiredTechnologies();

    SeasonName getSeason();
}
