package view.game;

import controller.game.GameEngine;
import model.game.building.BuildingType;
import model.game.hex.HexCoordinate;
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GameView extends JFrame {

    private final GameViewState viewState;
    private final GameHudPanel hudPanel;
    private final GameSidePanel sidePanel;
    private final HexActionPanel hexActionPanel;
    private final GameStatusPanel statusPanel;
    private final HexMapPanel mapPanel;
    private Runnable routeControlsRefreshHandler = () -> {};

    private ToastWindow toast;

    public GameView(GameEngine engine) {
        GameViewModel viewModel = new GameEngineViewModel(engine);

        this.viewState = new GameViewState();
        this.mapPanel = new HexMapPanel(viewModel, viewState);
        this.hudPanel = new GameHudPanel(viewModel);
        this.statusPanel = new GameStatusPanel();
        this.hexActionPanel = new HexActionPanel(
                viewModel,
                viewState,
                this::handleUnitSelectionChanged
        );
        this.sidePanel = new GameSidePanel(viewModel);

        configureFrame();
        assembleView();
    }

    private void configureFrame() {
        setTitle("Hex Civilization");
        setSize(1220, 780);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void assembleView() {
        add(hudPanel, BorderLayout.NORTH);
        JLayeredPane mapContainer = new JLayeredPane() {
            @Override
            public void doLayout() {
                mapPanel.setBounds(0, 0, getWidth(), getHeight());

                Dimension preferredSize =
                        hexActionPanel.getPreferredSize();
                int actionWidth = Math.min(
                        preferredSize.width,
                        Math.max(0, getWidth() - 24)
                );
                int actionHeight = Math.min(
                        preferredSize.height,
                        Math.max(0, getHeight() - 24)
                );
                int actionX = Math.max(
                        12,
                        getWidth() - actionWidth - 12
                );

                hexActionPanel.setBounds(
                        actionX,
                        12,
                        actionWidth,
                        actionHeight
                );
            }
        };
        mapContainer.setLayout(null);
        mapContainer.setOpaque(false);
        mapContainer.add(mapPanel, JLayeredPane.DEFAULT_LAYER);
        mapContainer.add(hexActionPanel, JLayeredPane.PALETTE_LAYER);
        add(mapContainer, BorderLayout.CENTER);
        JScrollPane sidePanelScroll = new JScrollPane(sidePanel);
        sidePanelScroll.setBorder(null);
        sidePanelScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        sidePanelScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(sidePanelScroll, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void handleUnitSelectionChanged() {
        hexActionPanel.refreshSelectionPanel();
        routeControlsRefreshHandler.run();
        mapPanel.repaint();
    }

    public void refresh() {
        hudPanel.refresh();
        hexActionPanel.refreshUnitCombo();
        hexActionPanel.refreshSelectionPanel();
        sidePanel.refreshTownHallQueue();
        mapPanel.repaint();
    }

    public void showHexActions() {
        hexActionPanel.openForSelection();
    }

    public void setHexClickHandler(
            Consumer<HexCoordinate> hexClickHandler
    ) {
        viewState.setHexClickHandler(hexClickHandler);
    }

    public JButton getBuildButton() {
        return hexActionPanel.getBuildMenuButton();
    }

    public JButton getConfirmBuildButton() {
        return hexActionPanel.getConfirmBuildButton();
    }

    public JButton getRuinButton() {
        return hexActionPanel.getRuinButton();
    }

    public JButton getStationButton() {
        return hexActionPanel.getStationButton();
    }

    public JButton getLevelUpButton() {
        return sidePanel.getLevelUpButton();
    }

    public JButton getAcquireTechnologyButton() {
        return sidePanel.getAcquireTechnologyButton();
    }

    public JButton getGenerateUnitButton() {
        return sidePanel.getGenerateUnitButton();
    }

    public JButton getExpandButton() {
        return hexActionPanel.getExpandButton();
    }

    public JButton getRouteButton() {
        return sidePanel.getRouteButton();
    }

    public JButton getClearRouteButton() {
        return sidePanel.getClearRouteButton();
    }

    public void setRouteControls(
            boolean unitSelected,
            boolean routeExists
    ) {
        sidePanel.setRouteControls(unitSelected, routeExists);
    }

    public void setRouteControlsRefreshHandler(Runnable handler) {
        routeControlsRefreshHandler = handler == null ? () -> {} : handler;
    }

    public JButton getEndTurnButton() {
        return sidePanel.getEndTurnButton();
    }

    public JButton getResetCameraButton() {
        return sidePanel.getResetCameraButton();
    }

    public Unit getSelectedUnit() {
        return viewState.getSelectedUnit();
    }

    public void setSelectedUnit(Unit selectedUnit) {
        viewState.setSelectedUnit(selectedUnit);
    }

    public HexCoordinate getSelectedHex() {
        return viewState.getSelectedHex();
    }

    public void setSelectedHex(HexCoordinate selectedHex) {
        viewState.setSelectedHex(selectedHex);
    }

    public BuildingType getSelectedBuildingType() {
        return hexActionPanel.getSelectedBuildingType();
    }

    public Level getSelectedLevel() {
        return sidePanel.getSelectedLevel();
    }

    public Technology getSelectedTechnology() {
        return sidePanel.getSelectedTechnology();
    }

    public UnitType getSelectedUnitType() {
        return sidePanel.getSelectedUnitType();
    }

    public void setStatus(String message) {
        statusPanel.setStatus(message);
    }

    public void setAlert(String message) {
        statusPanel.setAlert(message);
    }

    public void showToast(String message) {
        if (toast == null) {
            toast = new ToastWindow(this);
        }

        toast.show(message);
    }

    public void resetCamera() {
        mapPanel.resetCamera();
    }

    public void animateUnitMovement(
            Unit unit,
            HexCoordinate origin,
            HexCoordinate destination
    ) {
        mapPanel.animateUnitMovement(unit, origin, destination);
    }
}
