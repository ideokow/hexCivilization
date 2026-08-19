package controller.game;

import controller.game.system.BuildResult;
import controller.game.system.RuinStatus;
import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.MilitaryStable;
import model.game.building.ProductionBuilding;
import model.game.building.StationResult;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.townhall.TechnologyAcquireStatus;
import model.game.townhall.opration.AcquireTechnologyOperation;
import model.game.townhall.opration.GenerateCavalryOperation;
import model.game.townhall.opration.GenerateUnitOperation;
import model.game.townhall.opration.LevelUpOperation;
import model.game.townhall.opration.OperationCheckResult;
import model.game.townhall.opration.OperationStatus;
import model.game.townhall.opration.TownHallOperation;
import model.game.route.Route;
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
                engine.getPlayer(),
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
    Unit routing trigger
     */

    void route(Unit unit, HexCoordinate destination) {
        if (unit == null || destination == null || unit.getPosition() == null) {
            return;
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
        TownHallOperation operation = new LevelUpOperation(level, engine.getOperationQueue());
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
                    engine.getTownHall(),
                    engine.getPlayer()
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
        tribe.giftResource(resource);
        gameController.toastAlert("Gift sent!");
    }

    // peace req

    void requestPeaceTribe(Tribe tribe, Map<Resource, Integer> resource) {
        tribe.requestPeace(resource, engine.getTurnNumber());
        gameController.toastAlert("Request sent!");
    }

    // alliance req

    void requestAllianceTribe(Tribe tribe) {
        tribe.requestAlliance(engine.getTurnNumber());
        gameController.toastAlert("Request sent!");
    }

    // war dec

    void warDeclarationTribe(Tribe tribe) {
        tribe.warDeclaration();
        gameController.toastAlert("War declared!");
    }

    // acquire mission

    void acquireMission(Mission mission) {
        boolean status = mission.acquireMission(engine.getTurnNumber());
        gameController.toastAlert(status ? "Mission acquired successfully!" : "can't acquire mission.");
    }

    // cancel mission

    void cancelMission(Mission mission) {
        mission.cancelMission();
        gameController.toastAlert("Mission canceled!");
    }

    /*
    ...
     */
}
