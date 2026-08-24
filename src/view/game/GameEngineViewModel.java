package view.game;

import controller.game.GameEngine;
import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.Wall;
import model.game.season.SeasonName;
import model.game.townhall.Technology;
import model.game.townhall.operation.TownHallOperation;
import model.game.building.TribeCamp;
import model.game.tribe.Tribe;
import model.game.unit.Unit;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class GameEngineViewModel implements GameViewModel {

    private final GameEngine engine;

    GameEngineViewModel(GameEngine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    @Override
    public int getTurnNumber() {
        return engine.getTurnNumber();
    }

    @Override
    public TownHall getTownHall() {
        return engine.getTownHall();
    }

    @Override
    public Collection<Unit> getUnits() {
        return engine.getUnits().values();
    }

    @Override
    public boolean containsUnit(Unit unit) {
        return unit != null && engine.getUnits().containsValue(unit);
    }

    @Override
    public Hex getHex(HexCoordinate coordinate) {
        return engine.getHexGrid().get(coordinate);
    }

    @Override
    public List<Wall> getWalls(HexCoordinate coordinate) {
        if (coordinate == null) {
            return List.of();
        }
        return engine.getHexGrid().getWallLayer().getWalls(coordinate);
    }

    @Override
    public Set<Wall> getAllWalls() {
        return engine.getHexGrid().getWallLayer().getAllWalls();
    }

    @Override
    public Tribe getTribe(HexCoordinate coordinate) {
        Hex hex = getHex(coordinate);
        if (hex == null || !(hex.getBuilding() instanceof TribeCamp tribeCamp)) {
            return null;
        }
        return tribeCamp.getTribe();
    }

    @Override
    public boolean containsHex(HexCoordinate coordinate) {
        return engine.getHexGrid().contains(coordinate);
    }

    @Override
    public boolean isDiscovered(HexCoordinate coordinate) {
        return engine.getHexGrid().isDiscovered(coordinate);
    }

    @Override
    public boolean ownsTerritory(HexCoordinate coordinate) {
        return engine.getPlayer().ownsTerritory(coordinate);
    }

    @Override
    public boolean hasTradedAtBazaarThisTurn() {
        return engine.getTradeSystem().hasTradedAtBazaarThisTurn();
    }

    @Override
    public boolean hasTradedAtTradingPostThisTurn() {
        return engine.getTradeSystem().hasTradedAtTradingPostThisTurn();
    }

    @Override
    public boolean hasTradedWithTribeThisTurn(Tribe tribe) {
        return engine.getTradeSystem().hasTradedWithTribeThisTurn(tribe);
    }

    @Override
    public List<Hex> getHexesInBounds(
            int minQ,
            int maxQ,
            int minR,
            int maxR
    ) {
        return engine.getHexGrid().hexesInBounds(
                minQ,
                maxQ,
                minR,
                maxR
        );
    }

    @Override
    public int getNetResource(Resource resource) {
        return engine.getBuildingMap().getNetResource(
                resource,
                engine.getUnitMap(),
                engine.getSeason()
        );
    }

    @Override
    public TownHallOperation getInQueueOperation() {
        return engine.getInQueueOperation();
    }

    @Override
    public List<Technology> getAcquiredTechnologies() {
        return engine.getTownHall()
                .getTechnologies()
                .getAcquiredTechnologies();
    }

    @Override
    public SeasonName getSeason() {
        return engine.getSeason();
    }
}
