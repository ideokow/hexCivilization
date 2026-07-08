package controller.game;

import controller.game.system.*;
import model.game.building.TownHall;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.registry.BuildingRegistry;
import model.game.registry.UnitRegistry;
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
    private final StarvationSystem starvationSystem;
    private final TownHallWaiterSystem townHallWaiterSystem;

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
        BuildingRegistry.getInstance().addBuilding(townHall);

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
        starvationSystem = new StarvationSystem(townHall);
        townHallWaiterSystem = new TownHallWaiterSystem(townHall);

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

        // renew AP
        UnitRegistry.getInstance().renewUnitAPs();

        // generate resources
        Map<Resource, Integer> generatedResources = BuildingRegistry.getInstance().generateResources(townHall);
        // TODO: #UI resource added message

        // pay upkeep
        boolean upkeepStatus = BuildingRegistry.getInstance().payUpKeeps(townHall, hexGrid);
        // TODO: #UI show upkeep paying status

        // move in-way units
        routingSystem.moveUnits(inQueueRoutes);

        // feed units
        boolean feedStatus = starvationSystem.feedUnits();
        // TODO: #UI show feeding status

        // check starvation
        boolean starvation = starvationSystem.checkStarvationStatus();
        // TODO: #UI show starvation status

        // refresh upgrade queue
        townHallWaiterSystem.checkUpgrades();

        // TODO: #UI user listener
    }
}
