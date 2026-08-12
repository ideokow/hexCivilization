package controller.game;

import controller.game.system.*;
import model.game.building.*;
import model.game.hex.Hex;
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
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.townhall.TechnologyAcquireStatus;
import model.game.townhall.TownHall;
import model.game.townhall.opration.*;
import model.game.unit.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameEngine {

    public static final boolean DEBUG_VERBOSE = true;

    // singleton
    private static GameEngine instance;
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
    private final OperationQueue operationQueue;
    private final MovementSystem movementSystem;
    private GameController gameController;

    private final Map<Route, Unit> inQueueRoutes;
    private int turnNumber;

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

        // essential systems
        constructionSystem = new ConstructionSystem(hexGrid, townHall);
        operationQueue = new OperationQueue(townHall);
        movementSystem = new MovementSystem(hexGrid);
        routingSystem = new RoutingSystem(movementSystem);
        starvationSystem = new StarvationSystem(townHall);

        inQueueRoutes = new HashMap<>();
        turnNumber = 1;
        for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
            player.addTerritory(hexCoordinate);
        }
    }

    public void setController(GameController gameController) {
        this.gameController = gameController;
    }

    private void loadMap() {
        try {
            hexGrid = (new MapLoader()).loadMapX(3);
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

        // renew AP
        UnitRegistry.getInstance().renewUnitAPs(townHall.getHappiness().getEra());

        // generate resources
        Map<Resource, Integer> generatedResources = BuildingRegistry.getInstance().generateResources(townHall, getSeason());

        // pay upkeep
        UpKeepStatus upkeepStatus = BuildingRegistry.getInstance().payUpKeeps(townHall, hexGrid);

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

        // check is there monuments
        townHall.getHappiness().checkMonuments();
    }

    /*
    build caller and alert handler
     */
    public void buildTrigger(
        Unit unit,
        BuildingType buildingType,
        HexCoordinate hexCoordinate
    ) {
        BuildResult buildResult = constructionSystem.build(
                player,
                unit,
                buildingType,
                hexCoordinate
        );
        gameController.toastAlert(buildResult.getMessage());
    }

    /*
    destroy building trigger
     */
    public void ruinTrigger(
        Unit unit,
        Building building
    ) {
        RuinStatus ruinStatus = constructionSystem.ruin(
                unit,
                building
        );
        gameController.toastAlert(ruinStatus.getMessage());
    }

    /*
    unit station caller and alert handler
     */
    public void stationTrigger(Unit unit, HexCoordinate hexCoordinate) {
        if (hexCoordinate == null) {
            gameController.toastAlert(StationResult.WRONG_HEX.getMessage());
            return;
        };

        Hex hex = hexGrid.get(hexCoordinate);
        if (hex == null) {
            gameController.toastAlert(StationResult.WRONG_HEX.getMessage());
            return;
        };

        Building building = hex.getBuilding();

        if (!(building instanceof ProductionBuilding)) {
            gameController.toastAlert(StationResult.NOT_PRODUCTION_BUILDING.getMessage());
        }
        else if (!(unit instanceof Worker)) {
            gameController.toastAlert(StationResult.NOT_A_WORKER.getMessage());
        }
        else {
            StationResult stationResult = ((ProductionBuilding) building).stationWorker(((Worker) unit));
            gameController.toastAlert(stationResult.getMessage());
        }
    }

    /*
    route caller and alert handler
     */
    public void routeTrigger(Unit unit, HexCoordinate destination) {
        if (unit == null || destination == null || unit.getPosition() == null) {
            return;
        }

        Route route = routingSystem.route(unit.getPosition(), destination, hexGrid, gameController,
                getSeason(), canSail());
        if (route != null) {
            inQueueRoutes.entrySet().removeIf(entry -> entry.getValue().equals(unit));
            inQueueRoutes.put(route, unit);
        }
    }

    private boolean canSail() {
        return townHall.getTechnologies().isAcquired(Technology.BOAT_SAILING);
    }

    /*
    acquire technology trigger
     */
    public void acquireTechnologyTrigger(Technology technology) {
        TownHallOperation operation = new AcquireTechnologyOperation(technology, operationQueue);
        OperationCheckResult result = operationQueue.reserveOperation(operation);
        String message;
        if (!result.getStatus().equals(OperationStatus.POSSIBLE)) {
            message = result.getMessage();
        } else {
            message = TechnologyAcquireStatus.SUCCESS.getMessage();
        }
        gameController.toastAlert(message);
    }

    /*
    generate unit trigger
     */
    public void generateUnitTrigger(UnitType unitType) {
        TownHallOperation operation = new GenerateUnitOperation(unitType, operationQueue);
        OperationCheckResult result = operationQueue.reserveOperation(operation);
        String message;
        if (!result.getStatus().equals(OperationStatus.POSSIBLE)) {
            message = result.getMessage();
        } else {
            message = "Unit generation process started.";
        }
        gameController.toastAlert(message);
    }

    /*
    level up trigger
     */
    public void levelUpTrigger(Level level) {
        TownHallOperation operation = new LevelUpOperation(level, operationQueue);
        OperationCheckResult result = operationQueue.reserveOperation(operation);
        String message;
        if (!result.getStatus().equals(OperationStatus.POSSIBLE)) {
            message = result.getMessage();
        } else {
            message = "Level Up process started.";
        }
        gameController.toastAlert(message);
    }

    public TownHallOperation getInQueueOperation() {
        return operationQueue.getInQueueOperation();
    }

    /*
    expand trigger
     */
    public void expandTrigger(Unit unit) {
        if (!(unit instanceof BorderExpander)) {
            gameController.toastAlert("Select an expander.");
        }
        else {
            ((BorderExpander) unit).expand(hexGrid, townHall, player);
        }
    }

    public void clearRoute(Unit unit) {
        if (unit != null) {
            inQueueRoutes.entrySet().removeIf(entry -> entry.getValue().equals(unit));
        }
    }

    // UI getters

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
}
