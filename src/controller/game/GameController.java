package controller.game;

import controller.game.combat.CombatController;
import model.game.hex.HexCoordinate;
import model.game.hex.Hex;
import model.game.hex.Resource;
import model.game.building.MilitaryStable;
import model.game.building.TribeCamp;
import model.game.tribe.Tribe;
import model.game.registry.UpKeepStatus;
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.tribe.mission.Mission;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.military.MilitaryUnit;
import view.game.GameView;

import javax.swing.*;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameController {

    private final GameEngine engine;
    private final GameView view;
    private final GameTriggers triggers;
    private final Set<Unit> unitsWithRoutes = new HashSet<>();
    private boolean waitingForRouteDestination;
    private Unit routingUnit;
    private boolean waitingForCombatTarget;
    private HexCoordinate combatAttackCoordinate;

    public GameController(GameEngine engine, GameView view) {
        this.engine = engine;
        this.view = view;
        this.triggers = new GameTriggers(engine, this);
        attachListeners();
        view.setRouteControlsRefreshHandler(this::refreshRouteControls);
        view.refresh();
        refreshRouteControls();
    }

    private void attachListeners() {
        view.setHexClickHandler(this::handleHexClick);
        view.getEndTurnButton().addActionListener(e -> handleEndTurn());
        view.getConfirmBuildButton().addActionListener(e -> handleBuild());
        view.getRuinButton().addActionListener(e -> handleRuin());
        view.getLevelUpButton().addActionListener(e -> handleLevelUp());
        view.getAcquireTechnologyButton().addActionListener(e -> handleAcquireTechnology());
        view.getGenerateUnitButton().addActionListener(e -> handleGenerateUnit());
        view.getStationButton().addActionListener(e -> handleStationWorker());
        view.getGenerateMilitaryUnitButton().addActionListener(
                e -> handleGenerateMilitaryUnit()
        );
        view.getExpandButton().addActionListener(e -> handleExpand());
        view.getGiftTribeButton().addActionListener(e -> handleGiftTribe());
        view.getWarDeclarationTribeButton().addActionListener(e -> handleWarDeclarationTribe());
        view.getPeaceRequestTribeButton().addActionListener(e -> handlePeaceRequestTribe());
        view.getAllianceRequestTribeButton().addActionListener(e -> handleAllianceRequestTribe());
        view.getAcquireMissionButton().addActionListener(e -> handleAcquireMission());
        view.getCancelMissionButton().addActionListener(e -> handleCancelMission());
        view.getDeliverMissionButton().addActionListener(e -> handleDeliverMission());
        view.getRouteButton().addActionListener(e -> beginRouteSelection());
        view.getClearRouteButton().addActionListener(e -> clearSelectedRoute());
        view.getCombatButton().addActionListener(e -> beginCombatSelection());
        view.getResetCameraButton().addActionListener(e -> view.resetCamera());
    }

    // --- handlers ---

    private void handleHexClick(HexCoordinate coordinate) {

        if (waitingForRouteDestination) {
            finishRouteSelection(coordinate);
            return;
        }

        if (waitingForCombatTarget) {
            finishCombatSelection(coordinate);
            return;
        }

        // set selected hex in memory (for other functions)
        view.setSelectedHex(coordinate);

        // set selected units
        Hex clickedHex = engine.getHexGrid().get(coordinate);
        if (clickedHex == null) {
            return;
        }

        List<Unit> units = clickedHex.getUnits();
        view.setSelectedUnit(units.isEmpty() ? null : view.getSelectedUnit());

        // refresh
        view.refresh();
        if (engine.getHexGrid().isDiscovered(coordinate)
                && clickedHex.getBuilding() instanceof TribeCamp) {
            view.showTribeActions();
        }
        else {
            view.showHexActions();
        }
        refreshRouteControls();
    }

    private void exitSelectionMode() {
        waitingForRouteDestination = false;
        waitingForCombatTarget = false;
        combatAttackCoordinate = null;
        view.setAlert("");
    }

    private void beginCombatSelection() {
        HexCoordinate attackCoordinate = view.getSelectedHex();
        Hex attackHex = attackCoordinate == null
                ? null
                : engine.getHexGrid().get(attackCoordinate);

        if (attackHex == null || !hasPlayerMilitary(attackHex)) {
            view.showToast("Select a hex containing your military units.");
            return;
        }

        waitingForRouteDestination = false;
        routingUnit = null;
        combatAttackCoordinate = attackCoordinate;
        waitingForCombatTarget = true;
        view.setStatus("Select a discovered hex containing hostile military units.");
        view.setAlert("Choose combat target");
    }

    private void finishCombatSelection(HexCoordinate defenceCoordinate) {
        HexCoordinate attackCoordinate = combatAttackCoordinate;
        combatAttackCoordinate = null;
        waitingForCombatTarget = false;
        view.setAlert("");
        view.setStatus("");

        if (attackCoordinate == null || defenceCoordinate == null) {
            return;
        }

        Hex attackHex = engine.getHexGrid().get(attackCoordinate);
        Hex defenceHex = engine.getHexGrid().get(defenceCoordinate);
        if (attackHex == null || defenceHex == null) {
            return;
        }

        if (!engine.getHexGrid().isDiscovered(defenceCoordinate)) {
            view.showToast("You cannot attack an undiscovered hex.");
            return;
        }

        if (!hasHostileMilitary(defenceHex)) {
            view.showToast("The selected hex has no hostile military units.");
            return;
        }

        boolean targetTribe = defenceHex.getBuilding() instanceof TribeCamp;
        CombatController.launch(
                attackHex,
                defenceHex,
                targetTribe,
                () -> finishCombat(defenceCoordinate)
        );
    }

    private void finishCombat(HexCoordinate defenceCoordinate) {
        view.setSelectedHex(defenceCoordinate);
        view.setSelectedUnit(null);
        view.setStatus("Combat resolved.");
        view.refresh();
        refreshRouteControls();
    }

    private boolean hasPlayerMilitary(Hex hex) {
        return hex.getUnits().stream().anyMatch(
                unit -> unit instanceof MilitaryUnit && unit.isOwnedByPlayer()
        );
    }

    private boolean hasHostileMilitary(Hex hex) {
        return hex.getUnits().stream().anyMatch(
                unit -> unit instanceof MilitaryUnit && !unit.isOwnedByPlayer()
        );
    }

    private void beginRouteSelection() {

        if (waitingForRouteDestination) waitingForRouteDestination = false;

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

        triggers.route(unit, destination);
        unitsWithRoutes.add(unit);
    }

    public void animateUnitMovement(
            Unit unit,
            HexCoordinate origin,
            HexCoordinate destination
    ) {
        view.animateUnitMovement(unit, origin, destination);
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
        exitSelectionMode();

        // execution
        engine.executeTurn();
        view.refresh();
        refreshRouteControls();
    }

    private void handleBuild() {
        // cancel selection
        exitSelectionMode();

        triggers.build(
                view.getSelectedUnit(),
                view.getSelectedBuildingType(),
                view.getSelectedHex()
        );
        view.refresh();
    }

    private void handleRuin() {
        exitSelectionMode();

        HexCoordinate selectedHex = view.getSelectedHex();
        if (selectedHex == null) return;

        Hex hex = engine.getHexGrid().get(selectedHex);
        if (hex == null || hex.getBuilding() == null) return;

        int choice = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to ruin this building?",
                "Confirm Ruin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        triggers.ruin(view.getSelectedUnit(), hex.getBuilding());
        view.refresh();
    }

    private void handleStationWorker() {
        // cancel selection
        exitSelectionMode();

        triggers.station(view.getSelectedUnit(), view.getSelectedHex());
        view.refresh();
    }

    private void handleExpand() {
        // cancel selection
        exitSelectionMode();

        triggers.expand(view.getSelectedUnit());
        view.refresh();
    }

    private void handleGiftTribe() {
        Tribe tribe = view.getSelectedTribe();
        if (tribe != null) {
            triggers.giftTribe(tribe, view.getSelectedGiftResources());
            view.refresh();
        }
    }

    private void handleWarDeclarationTribe() {
        Tribe tribe = view.getSelectedTribe();
        if (tribe == null || tribe.isEnemy()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                view,
                "Declare war on this tribe?",
                "Confirm War Declaration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        triggers.warDeclarationTribe(tribe);
        view.refresh();
    }

    private void handlePeaceRequestTribe() {
        Tribe tribe = view.getSelectedTribe();
        if (tribe != null) {
            triggers.requestPeaceTribe(
                    tribe,
                    view.getPeaceRequestResources()
            );
            view.refresh();
        }
    }

    private void handleAllianceRequestTribe() {
        Tribe tribe = view.getSelectedTribe();
        if (tribe != null) {
            triggers.requestAllianceTribe(tribe);
            view.refresh();
        }
    }

    private void handleAcquireMission() {
        Mission mission = getSelectedMission();
        if (mission != null) {
            triggers.acquireMission(mission);
            view.refresh();
        }
    }

    private void handleCancelMission() {
        Mission mission = getSelectedMission();
        if (mission != null) {
            triggers.cancelMission(mission);
            view.refresh();
        }
    }

    private void handleDeliverMission() {
        Mission mission = getSelectedMission();
        if (mission != null) {
            triggers.deliverMission(mission);
            view.refresh();
        }
    }

    private Mission getSelectedMission() {
        Tribe tribe = view.getSelectedTribe();
        return tribe == null ? null : tribe.getCurrentMission();
    }

    private void handleLevelUp() {
        exitSelectionMode();

        Level level = view.getSelectedLevel();
        if (level != null) {
            triggers.levelUp(level);
        }
        view.refresh();
    }

    private void handleAcquireTechnology() {
        exitSelectionMode();

        Technology technology = view.getSelectedTechnology();
        if (technology != null) {
            triggers.acquireTechnology(technology);
        }
        view.refresh();
    }

    private void handleGenerateUnit() {
        exitSelectionMode();

        UnitType unitType = view.getSelectedUnitType();
        if (unitType != null) {
            triggers.generateUnit(unitType);
        }
        view.refresh();
    }

    private void handleGenerateMilitaryUnit() {
        exitSelectionMode();

        HexCoordinate selectedHex = view.getSelectedHex();
        if (selectedHex == null) {
            return;
        }

        Hex hex = engine.getHexGrid().get(selectedHex);
        if (hex == null || !(hex.getBuilding() instanceof MilitaryStable militaryStable)) {
            return;
        }

        triggers.generateMilitaryUnit(militaryStable);
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

    public void toastAlert(String message) {
        view.showToast(message);
    }
}
