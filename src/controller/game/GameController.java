package controller.game;

import controller.game.system.BuildResult;
import controller.game.system.MoveResult;
import model.game.building.BuildingType;
import model.game.building.StationResult;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.unit.Unit;
import model.game.unit.Worker;
import view.game.GameView;

import java.util.List;

public class GameController {

    public static final boolean DEBUG_VERBOSE = true;

    private final GameEngine engine;
    private final GameView view;

    public GameController(GameEngine engine, GameView view) {
        this.engine = engine;
        this.view = view;
        attachListeners();
        view.refresh();
    }

    private void attachListeners() {
        view.setHexClickHandler(this::handleHexClick);
        view.getEndTurnButton().addActionListener(e -> handleEndTurn());
        view.getBuildButton().addActionListener(e -> handleBuild());
        view.getStationButton().addActionListener(e -> handleStationWorker());
        view.getResetCameraButton().addActionListener(e -> view.resetCamera());
    }

    private void handleHexClick(HexCoordinate coordinate) {
        if (DEBUG_VERBOSE) System.out.println("click in <" + coordinate.getQ() + ", " + coordinate.getR() + ">");

        // set selected hex in memory (for other functions)
        view.setSelectedHex(coordinate);

        // set selected units
        List<Unit> units = engine.getHexGrid().get(coordinate).getUnits();
        view.setSelectedUnit(units.isEmpty() ? null : view.getSelectedUnit());

        // refresh
        view.refresh();
    }

    private void handleEndTurn() {
        engine.executeTurn();
        view.setStatus("Turn " + engine.getTurnNumber() + " started.");
        view.refresh();
    }

    private void handleBuild() {
        System.out.println("its build handle");
    }

    private void handleStationWorker() {
        System.out.println("its station handle");
    }
}
