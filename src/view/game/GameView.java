package view.game;

import controller.game.GameEngine;
import model.game.building.BuildingType;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.townhall.Level;
import model.game.townhall.Technology;
import model.game.tribe.Tribe;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

public class GameView extends JFrame {

    private final GameViewModel viewModel;
    private final GameViewState viewState;
    private final GameHudPanel hudPanel;
    private final GameSidePanel sidePanel;
    private final HexActionPanel hexActionPanel;
    private final TribeActionPanel tribeActionPanel;
    private final GameStatusPanel statusPanel;
    private final HexMapPanel mapPanel;
    private final SeasonOverlayPanel seasonOverlay;
    private Runnable routeControlsRefreshHandler = () -> {};

    private ToastWindow toast;

    public GameView(GameEngine engine) {
        this.viewModel = new GameEngineViewModel(engine);

        this.viewState = new GameViewState();
        this.mapPanel = new HexMapPanel(viewModel, viewState);
        this.hudPanel = new GameHudPanel(viewModel);
        this.statusPanel = new GameStatusPanel();
        this.seasonOverlay = new SeasonOverlayPanel();
        this.seasonOverlay.setSeason(viewModel.getSeason());
        this.hexActionPanel = new HexActionPanel(
                viewModel,
                viewState,
                this::handleUnitSelectionChanged
        );
        this.tribeActionPanel = new TribeActionPanel(viewModel, viewState);
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
                seasonOverlay.setBounds(0, 0, getWidth(), getHeight());

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

                Dimension tribePreferredSize =
                        tribeActionPanel.getPreferredSize();
                int tribeWidth = Math.min(
                        tribePreferredSize.width,
                        Math.max(0, getWidth() - 24)
                );
                int tribeHeight = Math.min(
                        tribePreferredSize.height,
                        Math.max(0, getHeight() - 24)
                );
                int tribeX = Math.max(
                        12,
                        getWidth() - tribeWidth - 12
                );

                tribeActionPanel.setBounds(
                        tribeX,
                        12,
                        tribeWidth,
                        tribeHeight
                );
            }
        };
        mapContainer.setLayout(null);
        mapContainer.setOpaque(false);
        mapContainer.add(mapPanel, JLayeredPane.DEFAULT_LAYER);
        mapContainer.add(seasonOverlay, JLayeredPane.PALETTE_LAYER);
        mapContainer.add(hexActionPanel, JLayeredPane.PALETTE_LAYER);
        mapContainer.add(tribeActionPanel, JLayeredPane.POPUP_LAYER);
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
        tribeActionPanel.refreshSelectionPanel();
        sidePanel.refreshTownHallQueue();
        seasonOverlay.setSeason(viewModel.getSeason());
        mapPanel.repaint();
    }

    public void showHexActions() {
        tribeActionPanel.setVisible(false);
        hexActionPanel.openForSelection();
    }

    public void showTribeActions() {
        hexActionPanel.setVisible(false);
        tribeActionPanel.openForSelection();
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

    public JButton getGiftTribeButton() {
        return tribeActionPanel.getGiftButton();
    }

    public JButton getWarDeclarationTribeButton() {
        return tribeActionPanel.getWarDeclarationButton();
    }

    public JButton getPeaceRequestTribeButton() {
        return tribeActionPanel.getPeaceRequestButton();
    }

    public JButton getAllianceRequestTribeButton() {
        return tribeActionPanel.getAllianceRequestButton();
    }

    public JButton getAcquireMissionButton() {
        return tribeActionPanel.getAcquireMissionButton();
    }

    public JButton getCancelMissionButton() {
        return tribeActionPanel.getCancelMissionButton();
    }

    public JButton getDeliverMissionButton() {
        return tribeActionPanel.getDeliverMissionButton();
    }

    public JButton getStationButton() {
        return hexActionPanel.getStationButton();
    }

    public JButton getGenerateMilitaryUnitButton() {
        return hexActionPanel.getGenerateMilitaryUnitButton();
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

    public JButton getCombatButton() {
        return hexActionPanel.getCombatButton();
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

    public Tribe getSelectedTribe() {
        return tribeActionPanel.getSelectedTribeForAction();
    }

    public Map<Resource, Integer> getSelectedGiftResources() {
        return tribeActionPanel.getSelectedGiftResources();
    }

    public Map<Resource, Integer> getPeaceRequestResources() {
        return tribeActionPanel.getPeaceRequestResources();
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
