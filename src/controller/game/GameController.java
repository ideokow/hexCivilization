package controller.game;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.registry.UpKeepStatus;
import model.game.unit.Unit;
import view.game.GameView;

import java.util.List;
import java.util.Map;

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

    // --- handlers ---

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
        view.refresh();
    }

    private void handleBuild() {
        System.out.println("its build handle");
    }

    private void handleStationWorker() {
        System.out.println("its station handle");
    }

    // --- alert triggers ---

    public void turnAlert(Map<Resource, Integer> generatedResources, UpKeepStatus upkeepStatus, boolean feedStatus) {
        int turn = engine.getTurnNumber();
        String message = MessageFormat.turnStatusAlert(turn, generatedResources, upkeepStatus, feedStatus);
        view.setStatus(message);
    }
}
