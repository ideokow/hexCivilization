package controller.game;

import controller.game.system.*;
import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.registry.BuildingRegistry;
import model.game.registry.UnitRegistry;
import model.game.registry.UpKeepStatus;
import model.game.route.Route;
import model.game.season.Season;
import model.game.season.SeasonName;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;
import model.game.townhall.opration.OperationQueue;
import model.game.townhall.opration.TownHallOperation;
import model.game.trade.TradeSystem;
import model.game.tribe.Tribe;
import model.game.unit.Unit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEngine {

    public static final boolean DEBUG_VERBOSE = false;
    public static final int MAP_NUMBER = 3;

    // singleton
    private static GameEngine instance;
    public static GameEngine getInstance() {
        if (instance == null) instance = new GameEngine();
        return instance;
    }

    private final Player player;

    private HexGrid hexGrid;
    private final TownHall townHall;
    private List<Tribe> tribes;
    private List<HexCoordinate> tradingPosts;

    private final ConstructionSystem constructionSystem;
    private final RoutingSystem routingSystem;
    private final StarvationSystem starvationSystem;
    private final OperationQueue operationQueue;
    private final TradeSystem tradeSystem;
    private final MovementSystem movementSystem;
    private GameController gameController;
    private final DisasterSpawner disasterSpawner;

    private final Map<Route, Unit> inQueueRoutes;
    private int turnNumber;

    private GameEngine() {

        HexCoordinate zero = new HexCoordinate(0, 0);

        // initialize player
        player = new Player();

        // initialize map
        loadMap();

        // initialize town hall
        townHall = new TownHall(hexGrid);
        hexGrid.get(zero).setBuilding(townHall);
        BuildingRegistry.getInstance().addBuilding(townHall);

        // essential systems
        constructionSystem = new ConstructionSystem(hexGrid, townHall);
        operationQueue = new OperationQueue(townHall);
        tradeSystem = new TradeSystem(townHall, player);
        movementSystem = new MovementSystem(hexGrid);
        routingSystem = new RoutingSystem(movementSystem);
        starvationSystem = new StarvationSystem(townHall);
        disasterSpawner = new DisasterSpawner(hexGrid);

        try {
            MapLoader mapLoader = new MapLoader();

            // load tribes
            tribes = mapLoader.loadTribes(MAP_NUMBER, townHall);
            mapLoader.loadTribesInGrid(hexGrid, tribes);

            // load trading posts
            tradingPosts = mapLoader.loadTradingPosts(MAP_NUMBER);
            mapLoader.loadTradingPostsInGrid(hexGrid, tradingPosts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        inQueueRoutes = new HashMap<>();
        turnNumber = 1;
        for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
            player.addTerritory(hexCoordinate);
        }
    }

    public void setController(GameController gameController) {
        this.gameController = gameController;
        disasterSpawner.setGameController(gameController);
    }

    private void loadMap() {
        try {
            hexGrid = (new MapLoader()).loadMap(MAP_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
            player.addTerritory(hexCoordinate);
        }
    }

    public void executeTurn() {
        if (DEBUG_VERBOSE) System.out.println("Turn > " + turnNumber);

        turnNumber++;
        tradeSystem.setCurrentTurn(turnNumber);

        // renew AP
        UnitRegistry.getInstance().renewUnitAPs(townHall.getHappiness().getEra());

        // generate resources
        Map<Resource, Integer> generatedResources = BuildingRegistry.getInstance().generateResources(townHall, getSeason());

        // pay upkeep
        UpKeepStatus upkeepStatus = BuildingRegistry.getInstance().payUpKeeps(townHall);

        // move in-way units
        routingSystem.moveUnits(inQueueRoutes, gameController, getSeason(), canSail());

        // feed units
        boolean feedStatus = starvationSystem.feedUnits();

        // tell ui each turn detail
        gameController.turnAlert(generatedResources, upkeepStatus, feedStatus);

        // check starvation
        boolean starvation = starvationSystem.checkStarvationStatus();
        gameController.starvationAlert(starvation);

        // operation queue
        operationQueue.handleTurn();

        // --- happiness ---
        // check is there monuments
        townHall.getHappiness().checkMonuments();
        // check is there military in TownHall
        townHall.getHappiness().checkTownHallMilitary(townHall, hexGrid);

        // trigger tribes
        tribes.forEach(tribe -> tribe.tick(turnNumber, movementSystem, hexGrid));

        // spawn disaster
        disasterSpawner.tick(getSeason());
    }

    // Getters

    public TownHallOperation getInQueueOperation() {
        return operationQueue.getInQueueOperation();
    }

    public TradeSystem getTradeSystem() {
        return tradeSystem;
    }

    public Player getPlayer() {
        return player;
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Map<String, Unit> getUnits() {
        return UnitRegistry.getInstance().getUnitMap();
    }

    public Map<String, Building> getBuildings() {
        return new HashMap<>(BuildingRegistry.getInstance().getBuildingMap());
    }

    public boolean isThereRoute(Unit unit) {
        return inQueueRoutes.containsValue(unit);
    }

    public SeasonName getSeason() {
        return Season.getSeason(turnNumber);
    }

    ConstructionSystem getConstructionSystem() {
        return constructionSystem;
    }

    RoutingSystem getRoutingSystem() {
        return routingSystem;
    }

    OperationQueue getOperationQueue() {
        return operationQueue;
    }

    boolean canSail() {
        return townHall.getTechnologies().isAcquired(Technology.BOAT_SAILING);
    }

    void queueRoute(Route route, Unit unit) {
        inQueueRoutes.entrySet().removeIf(entry -> entry.getValue().equals(unit));
        inQueueRoutes.put(route, unit);
    }

    public void clearRoute(Unit unit) {
        if (unit != null) {
            inQueueRoutes.entrySet().removeIf(entry -> entry.getValue().equals(unit));
        }
    }
}
