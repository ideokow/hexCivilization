package controller.game;

import controller.game.load.GameLoader;
import controller.game.map.*;
import controller.game.system.*;
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
    private final HexGrid hexGrid;
    private final TownHall townHall;
    private final List<Tribe> tribes;

    // state
    private final Map<Route, Unit> inQueueRoutes;
    private int turnNumber;

    // map
    private final BuildingMap buildingMap;
    private final UnitMap unitMap;

    // essential system
    private final ConstructionSystem constructionSystem = new ConstructionSystem(this);
    private final OperationQueue     operationQueue     = new OperationQueue();
    private final TradeSystem        tradeSystem        = new TradeSystem();
    private final DisasterSpawner    disasterSpawner    = new DisasterSpawner();
    private final StarvationSystem   starvationSystem   = new StarvationSystem();
    private final MovementSystem     movementSystem     = new MovementSystem();
    private final RoutingSystem      routingSystem      = new RoutingSystem(movementSystem);

    // controller
    private GameController gameController;

    private GameEngine() {
        GameLoader gameLoader = new GameLoader();
        gameLoader.loadGame(MAP_NUMBER);
        hexGrid       = gameLoader.getHexGrid();
        player        = gameLoader.getPlayer();
        buildingMap   = gameLoader.getBuildingMap();
        unitMap       = gameLoader.getUnitMap();
        townHall      = gameLoader.getTownHall();
        tribes        = gameLoader.getTribes();
        inQueueRoutes = gameLoader.getInQueueRoutes();
        turnNumber    = gameLoader.getTurnNumber();
    }

    public void setController(GameController gameController) {
        this.gameController = gameController;
        disasterSpawner.setGameController(gameController);
    }

    void executeTurn() {
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

    public List<Tribe> getTribes() {
        return tribes;
    }

    public BuildingMap getBuildingMap() {
        return buildingMap;
    }

    public UnitMap getUnitMap() {
        return unitMap;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public SeasonName getSeason() {
        return Season.getSeason(turnNumber);
    }

    boolean isThereRoute(Unit unit) {
        return inQueueRoutes.containsValue(unit);
    }

    public TownHallOperation getInQueueOperation() {
        return operationQueue.getInQueueOperation();
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

    Map<Route, Unit> getInQueueRoutes() {
        return inQueueRoutes;
    }

    boolean canSail() {
        return townHall.getTechnologies().isAcquired(Technology.BOAT_SAILING);
    }

    void clearRoute(Unit unit) {
        if (unit != null) {
            inQueueRoutes.entrySet().removeIf(entry -> entry.getValue().equals(unit));
        }
    }
}
