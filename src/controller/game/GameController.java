package controller.game;

import model.game.building.Upgrade;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.registry.UpKeepStatus;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import view.game.GameView;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameController {

    private final GameEngine engine;
    private final GameView view;
    private final Set<Unit> unitsWithRoutes = new HashSet<>();
    private boolean waitingForRouteDestination;
    private Unit routingUnit;

    public GameController(GameEngine engine, GameView view) {
        this.engine = engine;
        this.view = view;
        attachListeners();
        view.setRouteControlsRefreshHandler(this::refreshRouteControls);
        view.refresh();
        refreshRouteControls();
    }

    private void attachListeners() {
        view.setHexClickHandler(this::handleHexClick);
        view.getEndTurnButton().addActionListener(e -> handleEndTurn());
        view.getBuildButton().addActionListener(e -> handleBuild());
        view.getUpgradeButton().addActionListener(e -> handleUpgrade());
        view.getGenerateUnitButton().addActionListener(e -> handleGenerateUnit());
        view.getStationButton().addActionListener(e -> handleStationWorker());
        view.getRouteButton().addActionListener(e -> beginRouteSelection());
        view.getClearRouteButton().addActionListener(e -> clearSelectedRoute());
        view.getResetCameraButton().addActionListener(e -> view.resetCamera());
    }

    // --- handlers ---

    private void handleHexClick(HexCoordinate coordinate) {

        if (waitingForRouteDestination) {
            finishRouteSelection(coordinate);
            return;
        }

        // set selected hex in memory (for other functions)
        view.setSelectedHex(coordinate);

        // set selected units
        List<Unit> units = engine.getHexGrid().get(coordinate).getUnits();
        view.setSelectedUnit(units.isEmpty() ? null : view.getSelectedUnit());

        // refresh
        view.refresh();
        refreshRouteControls();
    }

    private void beginRouteSelection() {

        Unit selectedUnit = view.getSelectedUnit();

        if (selectedUnit == null) {
            return;
        }

        routingUnit = selectedUnit;
        waitingForRouteDestination = true;
        view.setStatus("Select a destination hex for the selected unit.");
    }

    private void finishRouteSelection(HexCoordinate destination) {
        Unit unit = routingUnit;
        routingUnit = null;
        waitingForRouteDestination = false;

        if (unit == null) {
            return;
        }

        routeUnit(unit, destination);
        view.setStatus("");
        view.refresh();
        refreshRouteControls();
    }

    private void clearSelectedRoute() {
        Unit selectedUnit = view.getSelectedUnit();

        if (selectedUnit == null) {
            return;
        }

        clearRoute(selectedUnit);
        refreshRouteControls();
    }

    /*
    Route hook used by the route-selection UI. Replace or extend this
    method when route ownership/cancellation is moved into the model.
     */
    public void routeUnit(Unit unit, HexCoordinate destination) {
        if (unit == null || destination == null) {
            return;
        }

        engine.routeTrigger(unit, destination);
        unitsWithRoutes.add(unit);
    }

    /* Route cancellation hook for the Clear Route button. */
    public void clearRoute(Unit unit) {
        if (unit != null) {
            engine.clearRoute(unit);
            unitsWithRoutes.remove(unit);
        }
    }

    public boolean hasRouteInUI(Unit unit) {
        return unit != null && unitsWithRoutes.contains(unit);
    }

    private void refreshRouteControls() {
        unitsWithRoutes.removeIf(unit -> !engine.isThereRoute(unit));

        Unit selectedUnit = view.getSelectedUnit();
        view.setRouteControls(
                selectedUnit != null,
                hasRouteInUI(selectedUnit)
        );
    }

    private void handleEndTurn() {
        // cancel selection
        waitingForRouteDestination = false;

        // execution
        engine.executeTurn();
        view.refresh();
        refreshRouteControls();
    }

    private void handleBuild() {
        // cancel selection
        waitingForRouteDestination = false;

        engine.buildTrigger(
                view.getSelectedUnit(),
                view.getSelectedBuildingType(),
                view.getSelectedHex()
        );
        view.refresh();
    }

    private void handleStationWorker() {
        // cancel selection
        waitingForRouteDestination = false;

        engine.stationTrigger(view.getSelectedUnit(), view.getSelectedHex());
        view.refresh();
    }

    private void handleUpgrade() {
        waitingForRouteDestination = false;

        engine.upgradeTrigger(view.getSelectedUpgrade());
        view.refresh();
    }

    private void handleGenerateUnit() {
        waitingForRouteDestination = false;

        engine.generateTrigger(view.getSelectedUnitType());
        view.refresh();
    }

    // --- alert triggers ---

    public void turnAlert(Map<Resource, Integer> generatedResources, UpKeepStatus upkeepStatus, boolean feedStatus) {
        int turn = engine.getTurnNumber();
        String message = MessageFormat.turnStatusAlert(turn, generatedResources, upkeepStatus, feedStatus);
        view.setStatus(message);
        view.setAlert("");
    }

    public void starvationAlert(boolean starvation) {
        if (starvation) {
            String message = MessageFormat.formatStarvationAlert(engine.getTurnNumber());
            view.setAlert(message);
        }
        else {
            view.setAlert("");
        }
    }

    public void townHallAlert(Upgrade upgrade, UnitType unitType) {
        if (upgrade != null) {
            toastAlert(MessageFormat.formatUpgradeAlert(upgrade));
        }
        if (unitType != null) {
            toastAlert(MessageFormat.formatGeneratedAlert(unitType));
        }
    }

    public void toastAlert(String message) {
        view.showToast(message);
    }
}
