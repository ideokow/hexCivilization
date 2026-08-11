package view.game;

import controller.game.GameEngine;
import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.season.SeasonName;
import model.game.registry.BuildingRegistry;
import model.game.townhall.Technology;
import model.game.townhall.opration.TownHallOperation;
import model.game.unit.Unit;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

final class GameEngineViewModel implements GameViewModel {

    private final GameEngine engine;
    private final BuildingRegistry buildingRegistry;

    GameEngineViewModel(GameEngine engine) {
        this.engine = Objects.requireNonNull(engine);
        this.buildingRegistry = BuildingRegistry.getInstance();
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
        return buildingRegistry.getNetResource(
                resource,
                engine.getTownHall(),
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
