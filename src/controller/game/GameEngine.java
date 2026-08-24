package controller.game;

import controller.game.load.MapLoader;
import controller.game.map.BuildingMap;
import controller.game.map.UnitMap;
import controller.game.map.UpKeepStatus;
import controller.game.system.*;
import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.player.Player;
import model.game.route.Route;
import model.game.season.Season;
import model.game.season.SeasonName;
import model.game.townhall.Technology;
import model.game.townhall.TownHall;
import model.game.townhall.operation.OperationQueue;
import model.game.townhall.operation.TownHallOperation;
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

    // parameter
    private final Player player;
    private HexGrid hexGrid;
    private final TownHall townHall;
    private List<Tribe> tribes;
    private List<HexCoordinate> tradingPosts;

    // state
    private final Map<Route, Unit> inQueueRoutes;
    private int turnNumber;

    // essential system
    private final ConstructionSystem constructionSystem = new ConstructionSystem(this);
    private final OperationQueue     operationQueue     = new OperationQueue();
    private final TradeSystem        tradeSystem        = new TradeSystem();
    private final DisasterSpawner    disasterSpawner    = new DisasterSpawner();
    private final StarvationSystem   starvationSystem   = new StarvationSystem();
    private final MovementSystem     movementSystem     = new MovementSystem();
    private final RoutingSystem      routingSystem      = new RoutingSystem(movementSystem);

    // map
    private final BuildingMap buildingMap;
    private final UnitMap unitMap;

    // controller
    private GameController gameController;

    private GameEngine() {

        HexCoordinate zero = new HexCoordinate(0, 0);

        // initialize player
        player = new Player();

        // initialize map
        loadMap();

        // load object maps
        buildingMap = new BuildingMap(hexGrid);
        unitMap = new UnitMap(hexGrid);

        // initialize town hall
        townHall = new TownHall(hexGrid, unitMap);
        hexGrid.get(zero).setBuilding(townHall);
        buildingMap.addBuilding(townHall);

        buildingMap.setTownHall(townHall);
        unitMap.setTownHall(townHall);

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
        unitMap.renewUnitAPs(townHall.getHappiness().getEra());

        // generate resources
        Map<Resource, Integer> generatedResources = buildingMap.generateResources(getSeason());

        // pay upkeep
        UpKeepStatus upkeepStatus = buildingMap.payUpKeeps();

        // move in-way units
        routingSystem.moveUnits(hexGrid, inQueueRoutes, gameController, getSeason(), canSail());

        // feed units
        boolean feedStatus = starvationSystem.feedUnits(townHall, unitMap);

        // tell ui each turn detail
        gameController.turnAlert(generatedResources, upkeepStatus, feedStatus);

        // check starvation
        boolean starvation = starvationSystem.checkStarvationStatus(townHall, buildingMap, unitMap);
        gameController.starvationAlert(starvation);

        // operation queue
        operationQueue.handleTurn();

        // --- happiness ---
        // check is there monuments
        townHall.getHappiness().checkMonuments(buildingMap);
        // check is there military in TownHall
        townHall.getHappiness().checkTownHallMilitary(townHall, hexGrid);

        // trigger tribes
        tribes.forEach(tribe -> tribe.tick(turnNumber, movementSystem, hexGrid));

        // spawn disaster
        disasterSpawner.tick(hexGrid, unitMap, buildingMap, getSeason());
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
        return unitMap.getMap();
    }

    public Map<String, Building> getBuildings() {
        return buildingMap.getMap();
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

    public BuildingMap getBuildingMap() {
        return buildingMap;
    }

    public UnitMap getUnitMap() {
        return unitMap;
    }
}
