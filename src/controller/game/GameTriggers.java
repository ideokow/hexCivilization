package controller.game;

import controller.game.system.BuildResult;
import controller.game.system.RuinStatus;
import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.Bazaar;
import model.game.building.MilitaryStable;
import model.game.building.ProductionBuilding;
import model.game.building.StationResult;
import model.game.building.TradingPost;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.Wall;
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.townhall.TechnologyAcquireStatus;
import model.game.townhall.operation.AcquireTechnologyOperation;
import model.game.townhall.operation.GenerateCavalryOperation;
import model.game.townhall.operation.GenerateUnitOperation;
import model.game.townhall.operation.LevelUpOperation;
import model.game.townhall.operation.OperationCheckResult;
import model.game.townhall.operation.OperationStatus;
import model.game.townhall.operation.TownHallOperation;
import model.game.route.Route;
import model.game.trade.TradeLevel;
import model.game.trade.TradeResult;
import model.game.tribe.Tribe;
import model.game.tribe.mission.Mission;
import model.game.unit.BorderExpander;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.Worker;

import java.util.Map;

final class GameTriggers {

    private final GameEngine engine;
    private final GameController gameController;

    GameTriggers(GameEngine engine, GameController gameController) {
        this.engine = engine;
        this.gameController = gameController;
    }

    /*
    Building related triggers
    - build
    - ruin
    - station
     */

    void build(
            Unit unit,
            BuildingType buildingType,
            HexCoordinate hexCoordinate
    ) {
        BuildResult buildResult = engine.getConstructionSystem().build(
                unit,
                buildingType,
                hexCoordinate
        );
        gameController.toastAlert(buildResult.getMessage());
    }

    void ruin(Unit unit, Building building) {
        RuinStatus ruinStatus = engine.getConstructionSystem().ruin(unit, building);
        gameController.toastAlert(ruinStatus.getMessage());
    }

    void station(Unit unit, HexCoordinate hexCoordinate) {
        if (hexCoordinate == null) {
            gameController.toastAlert(StationResult.WRONG_HEX.getMessage());
            return;
        }

        Hex hex = engine.getHexGrid().get(hexCoordinate);
        if (hex == null) {
            gameController.toastAlert(StationResult.WRONG_HEX.getMessage());
            return;
        }

        Building building = hex.getBuilding();
        if (!(building instanceof ProductionBuilding)) {
            gameController.toastAlert(StationResult.NOT_PRODUCTION_BUILDING.getMessage());
        }
        else if (!(unit instanceof Worker)) {
            gameController.toastAlert(StationResult.NOT_A_WORKER.getMessage());
        }
        else {
            StationResult stationResult = ((ProductionBuilding) building).stationWorker((Worker) unit);
            gameController.toastAlert(stationResult.getMessage());
        }
    }

    /*
    Building and Ruining wall
     */
    void build(
            Unit unit,
            HexCoordinate a,
            HexCoordinate b
    ) {
        BuildResult buildResult = engine.getConstructionSystem().build(unit, a, b);
        gameController.toastAlert(buildResult.getMessage());
    }

    void ruin(Unit unit, Wall wall) {
        RuinStatus ruinStatus = engine.getConstructionSystem().ruin(unit, wall);
        gameController.toastAlert(ruinStatus.getMessage());
    }

    /*
    Unit routing trigger
     */

    void route(Unit unit, HexCoordinate destination) {
        if (unit == null || destination == null || unit.getPosition() == null) {
            return;
        }

        if (!unit.isOwnedByPlayer()) {
            gameController.toastAlert("You cant route tribe's unit.");
        }

        Route route = engine.getRoutingSystem().route(
                unit.getPosition(),
                destination,
                engine.getHexGrid(),
                gameController,
                engine.getSeason(),
                engine.canSail()
        );
        if (route != null) {
            engine.queueRoute(route, unit);
        }
    }

    /*
    TownHall operation triggers:
    - Technology
    - Unit generation
    - Level
     */

    void acquireTechnology(Technology technology) {
        TownHallOperation operation = new AcquireTechnologyOperation(
                engine.getTownHall(),
                technology,
                engine.getOperationQueue()
        );
        OperationCheckResult result = engine.getOperationQueue().reserveOperation(operation);
        String message = result.getStatus().equals(OperationStatus.POSSIBLE)
                ? TechnologyAcquireStatus.SUCCESS.getMessage()
                : result.getMessage();
        gameController.toastAlert(message);
    }

    void generateUnit(UnitType unitType) {
        TownHallOperation operation = new GenerateUnitOperation(
                engine.getTownHall(),
                engine.getUnitMap(),
                unitType,
                engine.getOperationQueue()
        );
        OperationCheckResult result = engine.getOperationQueue().reserveOperation(operation);
        String message = result.getStatus().equals(OperationStatus.POSSIBLE)
                ? "Unit generation process started."
                : result.getMessage();
        gameController.toastAlert(message);
    }

    void levelUp(Level level) {
        TownHallOperation operation = new LevelUpOperation(engine.getTownHall(), level, engine.getOperationQueue());
        OperationCheckResult result = engine.getOperationQueue().reserveOperation(operation);
        String message = result.getStatus().equals(OperationStatus.POSSIBLE)
                ? "Level Up process started."
                : result.getMessage();
        gameController.toastAlert(message);
    }

    /*
    Buildings and Units special abilities
     */

    // Military stable generator trigger

    void generateMilitaryUnit(MilitaryStable militaryStable) {
        if (militaryStable == null) {
            return;
        }

        TownHallOperation operation = new GenerateCavalryOperation(
                engine.getTownHall(),
                engine.getUnitMap(),
                engine.getOperationQueue(),
                militaryStable
        );
        OperationCheckResult result = engine.getOperationQueue().reserveOperation(operation);
        String message = result.getStatus().equals(OperationStatus.POSSIBLE)
                ? "Unit generation process started."
                : result.getMessage();
        gameController.toastAlert(message);
    }

    // border expander trigger

    void expand(Unit unit) {
        if (!(unit instanceof BorderExpander)) {
            gameController.toastAlert("Select an expander.");
        }
        else {
            ((BorderExpander) unit).expand(
                    engine.getHexGrid(),
                    engine.getPlayer(),
                    engine.getUnitMap()
            );
        }
    }

    /*
    Tribe related triggers:
    - Tribe
    - Mission
     */

    // gift tribe

    void giftTribe(Tribe tribe, Map<Resource, Integer> resource) {
        if (tribe == null || !isValidResourceBundle(resource)) {
            gameController.toastAlert("Choose at least one valid resource to gift.");
            return;
        }
        if (tribe.isEnemy()) {
            gameController.toastAlert("Enemy tribes do not accept gifts.");
            return;
        }
        if (!engine.getTownHall().canAfford(resource)) {
            gameController.toastAlert("The Town Hall cannot afford this gift.");
            return;
        }

        engine.getTownHall().spendResources(resource);
        tribe.giftResource(resource);
        gameController.toastAlert("Gift sent!");
    }

    // peace req

    void requestPeaceTribe(Tribe tribe, Map<Resource, Integer> resource) {
        if (tribe == null
                || !tribe.canRequestPeace(resource, engine.getTurnNumber())) {
            gameController.toastAlert("Peace cannot be requested right now.");
            return;
        }
        if (!engine.getTownHall().canAfford(resource)) {
            gameController.toastAlert("The Town Hall cannot afford the peace payment.");
            return;
        }

        engine.getTownHall().spendResources(resource);
        tribe.requestPeace(resource, engine.getTurnNumber());
        gameController.toastAlert("Request sent!");
    }

    // alliance req

    void requestAllianceTribe(Tribe tribe) {
        if (tribe != null
                && tribe.requestAlliance(engine.getTurnNumber())) {
            gameController.toastAlert("Alliance request sent!");
        }
        else {
            gameController.toastAlert("Alliance cannot be requested right now.");
        }
    }

    // war dec

    void warDeclarationTribe(Tribe tribe) {
        if (tribe == null || tribe.isEnemy()) {
            return;
        }
        tribe.warDeclaration();
        gameController.toastAlert("War declared!");
    }

    private boolean isValidResourceBundle(Map<Resource, Integer> resource) {
        if (resource == null) {
            return false;
        }

        boolean hasResource = false;
        for (Resource value : Resource.values()) {
            int amount = resource.getOrDefault(value, 0);
            if (amount < 0) {
                return false;
            }
            hasResource |= amount > 0;
        }
        return hasResource;
    }

    // acquire mission

    void acquireMission(Mission mission) {
        if (mission == null) {
            return;
        }

        boolean status = mission.acquireMission(engine.getTurnNumber());
        gameController.toastAlert(status ? "Mission acquired successfully!" : "can't acquire mission.");
    }

    // cancel mission

    void cancelMission(Mission mission) {
        if (mission == null) {
            return;
        }

        mission.cancelMission();
        gameController.toastAlert("Mission canceled!");
    }

    // deliver mission

    void deliverMission(Mission mission) {
        if (mission == null) {
            return;
        }

        mission.finishMission(engine.getTownHall());
        gameController.toastAlert("Mission delivered!");
    }

    /*
    Trade related triggers
     */

    void tradeAtBazaar(
            Bazaar bazaar,
            Resource soldResource,
            Resource receivedResource,
            TradeLevel level
    ) {
        showTradeResult(engine.getTradeSystem().tradeAtBazaar(
                engine.getTownHall(),
                engine.getPlayer(),
                bazaar,
                soldResource,
                receivedResource,
                level
        ));
    }

    void tradeAtTradingPost(
            TradingPost tradingPost,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        showTradeResult(engine.getTradeSystem().tradeAtTradingPost(
                engine.getTownHall(),
                engine.getPlayer(),
                tradingPost,
                soldResource,
                receivedResource,
                amount
        ));
    }

    void tradeWithTribe(
            Tribe tribe,
            Resource soldResource,
            Resource receivedResource,
            int amount
    ) {
        showTradeResult(engine.getTradeSystem().tradeWithTribe(
                engine.getTownHall(),
                tribe,
                soldResource,
                receivedResource,
                amount
        ));
    }

    private void showTradeResult(TradeResult result) {
        if (result == null) {
            gameController.toastAlert("Trade could not be completed.");
        } else if (!result.isSuccess()) {
            gameController.toastAlert(result.getMessage());
        } else {
            gameController.toastAlert(
                    "Trade completed: -"
                            + result.getSoldAmount()
                            + " sold, +"
                            + result.getAddedAmount()
                            + " received at "
                            + result.getConversionRatePercent()
                            + "%."
            );
        }
    }
}
