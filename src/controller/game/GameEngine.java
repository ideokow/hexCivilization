package controller.game;

import model.game.building.TownHall;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.player.Player;
import model.game.route.Route;
import model.game.unit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameEngine {

    // singleton
    public static GameEngine instance;
    public static GameEngine getInstance() {
        if (instance == null) instance = new GameEngine();
        return instance;
    }

    private final Player player;

    private HexGrid hexGrid;
    private final TownHall townHall;

    private final ConstructionSystem constructionSystem;
    private final RoutingSystem routingSystem;

    private final Map<Route, Unit> inQueueRoutes;

    private GameEngine() {

        HexCoordinate zero = new HexCoordinate(0, 0);

        // initialize player
        player = new Player();

        // initialize map
        loadMap();

        // initialize town hall
        townHall = new TownHall(hexGrid, player);
        hexGrid.get(zero).setBuilding(townHall);

        // initial units
        townHall.generateUnit(UnitType.BUILDER);
        townHall.generateUnit(UnitType.BUILDER);
        townHall.generateUnit(UnitType.WORKER);
        townHall.generateUnit(UnitType.WORKER);
        townHall.generateUnit(UnitType.EXPLORER);

        // essential systems
        constructionSystem = new ConstructionSystem(hexGrid, townHall);
        MovementSystem movementSystem = new MovementSystem(hexGrid);
        routingSystem = new RoutingSystem(movementSystem);

        inQueueRoutes = new HashMap<>();
    }

    private void loadMap() {
        try {
            hexGrid = (new MapLoader()).loadMap01();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
            player.addTerritory(hexCoordinate);
        }
    }

    public void executeTurn() {
        // TODO: renew AP
        // TODO: generate resource
        // TODO: pay upkeep

        // move in-way units
        routingSystem.moveUnits(inQueueRoutes);

        // TODO: user listener
    }
}
