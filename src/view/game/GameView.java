package view.game;

import controller.game.GameEngine;
import controller.game.system.StarvationSystem;
import model.game.building.Building;
import model.game.building.BuildingType;
import model.game.building.PopulationType;
import model.game.building.ProductionBuilding;
import model.game.building.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.TerrainType;
import model.game.registry.BuildingRegistry;
import model.game.unit.Builder;
import model.game.unit.Unit;
import model.game.unit.UnitType;
import model.game.unit.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class GameView extends JFrame {

    private static final Resource[] STORED_RESOURCES = {
            Resource.FOOD,
            Resource.WOOD,
            Resource.STONE,
            Resource.IRON
    };

    private final GameEngine engine;
    private final HexMapPanel mapPanel;
    private final JLabel turnLabel;
    private final JLabel unitsLabel;
    private final JLabel unitBreakdownLabel;
    private final Map<Resource, JLabel> resourceLabels;
    private final JTextArea selectedInfoArea;
    private final JLabel statusLabel;
    private final JComboBox<Unit> selectedUnitCombo;
    private final DefaultComboBoxModel<Unit> selectedUnitModel;
    private final JComboBox<BuildingType> buildingCombo;
    private final JButton buildButton;
    private final JButton stationButton;
    private final JButton endTurnButton;
    private final JButton resetCameraButton;

    private Consumer<HexCoordinate> hexClickHandler;
    private HexCoordinate selectedHex;
    private Unit selectedUnit;
    private boolean updatingUnitCombo;

    public GameView(GameEngine engine) {
        this.engine = engine;
        this.mapPanel = new HexMapPanel();
        this.turnLabel = new JLabel();
        this.unitsLabel = new JLabel();
        this.unitBreakdownLabel = new JLabel();
        this.resourceLabels = new EnumMap<>(Resource.class);
        this.selectedInfoArea = new JTextArea();
        this.statusLabel = new JLabel("Select a hex. Select a unit, then click an adjacent discovered hex to move.");
        this.selectedUnitModel = new DefaultComboBoxModel<>();
        this.selectedUnitCombo = new JComboBox<>(selectedUnitModel);
        this.buildingCombo = new JComboBox<>(new BuildingType[]{
                BuildingType.LUMBER_MILL,
                BuildingType.STONE_MINE,
                BuildingType.IRON_MINE,
                BuildingType.FARM,
                BuildingType.STABLE,
                BuildingType.VILLAGE,
                BuildingType.TOWN
        });
        this.buildButton = new JButton("Build");
        this.stationButton = new JButton("Station Worker");
        this.endTurnButton = new JButton("End Turn");
        this.resetCameraButton = new JButton("Reset Camera");

        configureFrame();
        add(buildHudPanel(), BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private void configureFrame() {
        setTitle("Hex Civilization");
        setSize(1220, 780);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private JPanel buildHudPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        panel.setBackground(new Color(28, 35, 48));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);
        turnLabel.setForeground(new Color(238, 241, 247));
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        leftPanel.add(turnLabel);

        unitsLabel.setForeground(new Color(238, 241, 247));
        unitsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        leftPanel.add(unitsLabel);

        unitBreakdownLabel.setForeground(new Color(188, 198, 215));
        unitBreakdownLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        leftPanel.add(unitBreakdownLabel);

        JPanel resourcePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        resourcePanel.setOpaque(false);
        for (Resource resource : STORED_RESOURCES) {
            JLabel resourceLabel = new JLabel();
            resourceLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            resourceLabels.put(resource, resourceLabel);
            resourcePanel.add(resourceLabel);
        }

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(resourcePanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(292, 100));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(236, 239, 244));

        JLabel selectedTitle = new JLabel("Selection");
        selectedTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        selectedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(selectedTitle);
        panel.add(Box.createVerticalStrut(8));

        selectedInfoArea.setEditable(false);
        selectedInfoArea.setOpaque(false);
        selectedInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        selectedInfoArea.setLineWrap(true);
        selectedInfoArea.setWrapStyleWord(true);
        selectedInfoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(selectedInfoArea);
        panel.add(Box.createVerticalStrut(12));

        JLabel unitLabel = new JLabel("Unit on selected hex");
        unitLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(unitLabel);
        panel.add(Box.createVerticalStrut(4));

        selectedUnitCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        selectedUnitCombo.setRenderer(new UnitRenderer());
        selectedUnitCombo.addActionListener(event -> {
            if (!updatingUnitCombo) {
                selectedUnit = (Unit) selectedUnitCombo.getSelectedItem();
                mapPanel.repaint();
                refreshSelectionPanel();
            }
        });
        panel.add(selectedUnitCombo);
        panel.add(Box.createVerticalStrut(16));

        JLabel actionsTitle = new JLabel("Actions");
        actionsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(actionsTitle);
        panel.add(Box.createVerticalStrut(8));

        buildingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(buildingCombo);
        panel.add(Box.createVerticalStrut(6));

        buildButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(buildButton);
        panel.add(Box.createVerticalStrut(6));

        stationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(stationButton);
        panel.add(Box.createVerticalStrut(6));

        endTurnButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        endTurnButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(endTurnButton);
        panel.add(Box.createVerticalStrut(6));

        resetCameraButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetCameraButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        panel.add(resetCameraButton);
        panel.add(Box.createVerticalStrut(18));

        JLabel legendTitle = new JLabel("Legend");
        legendTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        legendTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(legendTitle);
        panel.add(Box.createVerticalStrut(8));

        JTextArea legendArea = new JTextArea("""
                Drag: pan camera
                Mouse wheel: discrete zoom
                Gold border: territory
                Dark hex: fog of war
                Colored dots: units
                TH/LM/SM/IM/FM/ST: buildings
                """);
        legendArea.setEditable(false);
        legendArea.setOpaque(false);
        legendArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        legendArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(legendArea);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        panel.setBackground(new Color(22, 27, 36));

        statusLabel.setForeground(new Color(228, 232, 240));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(statusLabel, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        refreshHud();
        refreshUnitCombo();
        refreshSelectionPanel();
        mapPanel.repaint();
    }

    private void refreshHud() {
        TownHall townHall = engine.getTownHall();
        turnLabel.setText("Turn " + engine.getTurnNumber());
        unitsLabel.setText("Units " + townHall.getUnitNumber() + "/" + townHall.getUnitCap());
        unitBreakdownLabel.setText(buildUnitBreakdown());

        Map<Resource, Integer> storage = townHall.getResourceStorage();
        for (Resource resource : STORED_RESOURCES) {
            int amount = storage.getOrDefault(resource, 0);
            int net = calculateNetResource(resource);
            String netText = (net >= 0 ? "+" : "") + net + "/turn";
            String netColor = net < 0 ? "#d33f49" : "#3cb371";
            resourceLabels.get(resource).setText("<html><span style='color:#eef1f7'>"
                    + resource.getDisplayName() + " " + amount + "/" + townHall.getResourceCap()
                    + "</span> <span style='color:" + netColor + "'>" + netText + "</span></html>");
        }
    }

    private String buildUnitBreakdown() {
        Map<UnitType, Integer> counts = new EnumMap<>(UnitType.class);
        for (UnitType unitType : UnitType.values()) {
            counts.put(unitType, 0);
        }
        for (Unit unit : engine.getUnits().values()) {
            counts.put(unit.getType(), counts.get(unit.getType()) + 1);
        }

        List<String> parts = new ArrayList<>();
        for (UnitType unitType : UnitType.values()) {
            int count = counts.get(unitType);
            if (count > 0) {
                parts.add(pretty(unitType) + " " + count);
            }
        }
        return String.join("  |  ", parts);
    }

    private int calculateNetResource(Resource resource) {
        return BuildingRegistry.getInstance().getNetResource(resource, GameEngine.getInstance().getTownHall());
    }

    private void refreshUnitCombo() {
        updatingUnitCombo = true;
        selectedUnitModel.removeAllElements();

        if (selectedHex != null) {
            Hex hex = engine.getHexGrid().get(selectedHex);
            if (hex != null) {
                for (Unit unit : hex.getUnits()) {
                    selectedUnitModel.addElement(unit);
                }
            }
        }

        if (selectedUnit != null && engine.getUnits().containsKey(selectedUnit.getUnitID())) {
            selectedUnitCombo.setSelectedItem(selectedUnit);
        } else if (selectedUnitModel.getSize() > 0) {
            selectedUnit = selectedUnitModel.getElementAt(0);
            selectedUnitCombo.setSelectedItem(selectedUnit);
        } else {
            selectedUnit = null;
        }

        selectedUnitCombo.setEnabled(selectedUnitModel.getSize() > 0);
        updatingUnitCombo = false;
    }

    private void refreshSelectionPanel() {
        if (selectedHex == null) {
            selectedInfoArea.setText("No hex selected.");
            return;
        }

        Hex hex = engine.getHexGrid().get(selectedHex);
        if (hex == null) {
            selectedInfoArea.setText("Selected hex is outside the map.");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append("Hex: ").append(formatCoordinate(selectedHex)).append('\n');
        text.append("Discovered: ").append(engine.getHexGrid().isDiscovered(selectedHex) ? "Yes" : "No").append('\n');
        text.append("Territory: ").append(engine.getPlayer().ownsTerritory(selectedHex) ? "Yes" : "No").append('\n');

        if (engine.getHexGrid().isDiscovered(selectedHex)) {
            text.append("Terrain: ").append(pretty(hex.getTerrain())).append('\n');
            text.append("Move AP: ").append(hex.getTerrain().getMovementCost()).append('\n');
            text.append("Resources: ").append(formatResources(hex.getAvailableResources())).append('\n');
            text.append("Building: ").append(formatBuilding(hex.getBuilding())).append('\n');
            text.append("Units: ").append(hex.getUnits().size()).append('\n');
        } else {
            text.append("Terrain: Unknown\n");
            text.append("Resources: Hidden by fog\n");
        }

        if (selectedUnit != null) {
            text.append('\n');
            text.append("Selected Unit\n");
            text.append("Type: ").append(pretty(selectedUnit.getType())).append('\n');
            text.append("AP: ").append(selectedUnit.getCurrentAP())
                    .append('/').append(selectedUnit.getType().getEachTurnAP()).append('\n');
            text.append("Vision: ").append(selectedUnit.getType().getVisibilityRadius()).append('\n');
            if (selectedUnit instanceof Builder builder) {
                text.append("Charges: ").append(builder.getCharges()).append('\n');
            }
            if (selectedUnit instanceof Worker worker) {
                text.append("Stationed: ").append(worker.isInBuilding() ? "Yes" : "No").append('\n');
            }
        }

        selectedInfoArea.setText(text.toString());
    }

    public void setHexClickHandler(Consumer<HexCoordinate> hexClickHandler) {
        this.hexClickHandler = hexClickHandler;
    }

    public JButton getBuildButton() {
        return buildButton;
    }

    public JButton getStationButton() {
        return stationButton;
    }

    public JButton getEndTurnButton() {
        return endTurnButton;
    }

    public JButton getResetCameraButton() {
        return resetCameraButton;
    }

    public Unit getSelectedUnit() {
        return selectedUnit;
    }

    public void setSelectedUnit(Unit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    public HexCoordinate getSelectedHex() {
        return selectedHex;
    }

    public void setSelectedHex(HexCoordinate selectedHex) {
        this.selectedHex = selectedHex;
    }

    public BuildingType getSelectedBuildingType() {
        return (BuildingType) buildingCombo.getSelectedItem();
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public void resetCamera() {
        mapPanel.resetCamera();
    }

    public void animateUnitMovement(Unit unit, HexCoordinate origin, HexCoordinate destination) {
        mapPanel.animateUnitMovement(unit, origin, destination);
    }

    private String formatCoordinate(HexCoordinate coordinate) {
        return "(" + coordinate.getQ() + ", " + coordinate.getR() + ")";
    }

    private String formatResources(Set<Resource> resources) {
        if (resources.isEmpty()) {
            return "None";
        }

        List<String> resourceNames = new ArrayList<>();
        for (Resource resource : resources) {
            resourceNames.add(resource.getDisplayName());
        }
        resourceNames.sort(String::compareTo);
        return String.join(", ", resourceNames);
    }

    private String formatBuilding(Building building) {
        if (building == null) {
            return "None";
        }
        return pretty(building.getType()) + (building.isRuined() ? " (Ruined)" : "");
    }

    private String pretty(Enum<?> value) {
        String text = value.name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder(text.length());
        boolean capitalize = true;
        for (char character : text.toCharArray()) {
            if (capitalize && Character.isLetter(character)) {
                result.append(Character.toUpperCase(character));
                capitalize = false;
            } else {
                result.append(character);
            }
            if (character == ' ') {
                capitalize = true;
            }
        }
        return result.toString();
    }

    private final class HexMapPanel extends JPanel {

        private static final double ROOT_THREE = 1.7320508075688772;
        private static final int BASE_HEX_SIZE = 32;
        private static final int UNIT_ANIMATION_FRAMES = 14;
        private final double[] zoomLevels = {0.70, 0.85, 1.0, 1.20, 1.45, 1.75, 2.10};
        private final Map<HexCoordinate, Polygon> screenHexes = new HashMap<>();

        private int zoomIndex = 2;
        private int panOffsetX;
        private int panOffsetY;
        private Point lastDragPoint;
        private int dragDistance;

        private Unit movingUnit;
        private HexCoordinate animationOrigin;
        private HexCoordinate animationDestination;
        private int animationFrame;
        private Timer animationTimer;

        HexMapPanel() {
            setBackground(new Color(18, 22, 29));
            setFocusable(true);

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    requestFocusInWindow();
                    lastDragPoint = mouseEvent.getPoint();
                    dragDistance = 0;
                }

                @Override
                public void mouseDragged(MouseEvent mouseEvent) {
                    if (lastDragPoint == null) {
                        return;
                    }

                    int deltaX = mouseEvent.getX() - lastDragPoint.x;
                    int deltaY = mouseEvent.getY() - lastDragPoint.y;
                    panOffsetX += deltaX;
                    panOffsetY += deltaY;
                    dragDistance += Math.abs(deltaX) + Math.abs(deltaY);
                    lastDragPoint = mouseEvent.getPoint();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    lastDragPoint = null;
                }

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (dragDistance > 8) {
                        return;
                    }

                    HexCoordinate clickedCoordinate = getCoordinateAt(mouseEvent.getPoint());
                    if (clickedCoordinate != null && hexClickHandler != null) {
                        hexClickHandler.accept(clickedCoordinate);
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                    zoomBy(mouseWheelEvent.getWheelRotation() < 0 ? 1 : -1);
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
            addMouseWheelListener(mouseAdapter);
        }

        void resetCamera() {
            panOffsetX = 0;
            panOffsetY = 0;
            zoomIndex = 2;
            repaint();
        }

        void animateUnitMovement(Unit unit, HexCoordinate origin, HexCoordinate destination) {
            movingUnit = unit;
            animationOrigin = origin;
            animationDestination = destination;
            animationFrame = 0;

            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            animationTimer = new Timer(24, timerEvent -> {
                animationFrame++;
                if (animationFrame >= UNIT_ANIMATION_FRAMES) {
                    movingUnit = null;
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        }

        private void zoomBy(int direction) {
            int nextZoomIndex = Math.max(0, Math.min(zoomLevels.length - 1, zoomIndex + direction));
            if (nextZoomIndex != zoomIndex) {
                zoomIndex = nextZoomIndex;
                repaint();
            }
        }

        private HexCoordinate getCoordinateAt(Point point) {
            for (Map.Entry<HexCoordinate, Polygon> entry : screenHexes.entrySet()) {
                if (entry.getValue().contains(point)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawBackground(graphics2D);
            drawHexes(graphics2D);
            drawUnits(graphics2D);
            drawMovingUnit(graphics2D);
            drawMapInstructions(graphics2D);

            graphics2D.dispose();
        }

        private void drawBackground(Graphics2D graphics2D) {
            graphics2D.setPaint(new GradientPaint(
                    0, 0, new Color(24, 31, 43),
                    0, getHeight(), new Color(12, 15, 21)));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
        }

        private void drawHexes(Graphics2D graphics2D) {
            screenHexes.clear();
            List<Hex> hexes = engine.getHexGrid().getAllHexes();
            hexes.sort(Comparator
                    .comparingInt((Hex hex) -> hex.getCoordinate().getR())
                    .thenComparingInt(hex -> hex.getCoordinate().getQ()));

            for (Hex hex : hexes) {
                Polygon polygon = createHexPolygon(hex.getCoordinate());
                screenHexes.put(hex.getCoordinate(), polygon);

                boolean discovered = engine.getHexGrid().isDiscovered(hex.getCoordinate());
                Color fillColor = discovered ? terrainColor(hex) : new Color(29, 33, 43);
                graphics2D.setColor(fillColor);
                graphics2D.fillPolygon(polygon);

                if (engine.getPlayer().ownsTerritory(hex.getCoordinate())) {
                    graphics2D.setColor(new Color(231, 184, 77));
                    graphics2D.setStroke(new BasicStroke(2.2f));
                } else {
                    graphics2D.setColor(discovered ? new Color(70, 80, 92) : new Color(43, 48, 58));
                    graphics2D.setStroke(new BasicStroke(1.0f));
                }
                graphics2D.drawPolygon(polygon);

                if (selectedHex != null && selectedHex.equals(hex.getCoordinate())) {
                    graphics2D.setColor(new Color(255, 255, 255, 180));
                    graphics2D.setStroke(new BasicStroke(3.0f));
                    graphics2D.drawPolygon(polygon);
                }

                if (discovered) {
                    drawHexContent(graphics2D, hex);
                } else {
                    drawFogMark(graphics2D, polygon);
                }
            }
        }

        private void drawHexContent(Graphics2D graphics2D, Hex hex) {
            Point2D.Double center = centerOf(hex.getCoordinate());
            double hexSize = currentHexSize();

            graphics2D.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, (int) (11 * currentZoom()))));
            graphics2D.setColor(new Color(25, 30, 37, 180));
            String terrainLabel = terrainShortName(hex.getTerrain());
            drawCenteredString(graphics2D, terrainLabel, center.x, center.y - hexSize * 0.15);

            if (!hex.getAvailableResources().isEmpty()) {
                graphics2D.setColor(new Color(255, 255, 255, 215));
                graphics2D.fillRoundRect(
                        (int) (center.x - hexSize * 0.48),
                        (int) (center.y + hexSize * 0.12),
                        (int) (hexSize * 0.96),
                        (int) (hexSize * 0.34),
                        12,
                        12);
                graphics2D.setColor(new Color(54, 58, 64));
                graphics2D.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (10 * currentZoom()))));
                drawCenteredString(graphics2D, resourcesShortName(hex.getAvailableResources()), center.x, center.y + hexSize * 0.36);
            }

            if (hex.getBuilding() != null) {
                graphics2D.setColor(new Color(30, 34, 42, 220));
                graphics2D.fillOval((int) (center.x - hexSize * 0.28), (int) (center.y - hexSize * 0.92),
                        (int) (hexSize * 0.56), (int) (hexSize * 0.36));
                graphics2D.setColor(Color.WHITE);
                graphics2D.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (10 * currentZoom()))));
                drawCenteredString(graphics2D, buildingShortName(hex.getBuilding().getType()), center.x, center.y - hexSize * 0.66);
            }
        }

        private void drawFogMark(Graphics2D graphics2D, Polygon polygon) {
            Rectangle bounds = polygon.getBounds();
            graphics2D.setColor(new Color(7, 9, 13, 95));
            graphics2D.fillPolygon(polygon);
            graphics2D.setColor(new Color(95, 103, 118, 90));
            graphics2D.setFont(new Font("SansSerif", Font.BOLD, 14));
            drawCenteredString(graphics2D, "?", bounds.getCenterX(), bounds.getCenterY() + 5);
        }

        private void drawUnits(Graphics2D graphics2D) {
            for (Hex hex : engine.getHexGrid().getAllHexes()) {
                if (!engine.getHexGrid().isDiscovered(hex.getCoordinate())) {
                    continue;
                }

                List<Unit> units = hex.getUnits();
                for (int unitIndex = 0; unitIndex < units.size(); unitIndex++) {
                    Unit unit = units.get(unitIndex);
                    if (unit.equals(movingUnit)) {
                        continue;
                    }
                    drawUnit(graphics2D, unit, hex.getCoordinate(), unitIndex, units.size());
                }
            }
        }

        private void drawMovingUnit(Graphics2D graphics2D) {
            if (movingUnit == null || animationOrigin == null || animationDestination == null) {
                return;
            }

            Point2D.Double originCenter = centerOf(animationOrigin);
            Point2D.Double destinationCenter = centerOf(animationDestination);
            double progress = Math.min(1.0, animationFrame / (double) UNIT_ANIMATION_FRAMES);
            double smoothProgress = progress * progress * (3.0 - 2.0 * progress);
            double screenX = originCenter.x + (destinationCenter.x - originCenter.x) * smoothProgress;
            double screenY = originCenter.y + (destinationCenter.y - originCenter.y) * smoothProgress;
            drawUnitAt(graphics2D, movingUnit, screenX, screenY, true);
        }

        private void drawUnit(Graphics2D graphics2D, Unit unit, HexCoordinate coordinate, int unitIndex, int unitsInHex) {
            Point2D.Double center = centerOf(coordinate);
            double unitRadius = currentHexSize() * 0.19;
            double angle = (Math.PI * 2.0 * unitIndex) / Math.max(1, unitsInHex);
            double spread = unitsInHex == 1 ? 0 : currentHexSize() * 0.33;
            double screenX = center.x + Math.cos(angle) * spread;
            double screenY = center.y + Math.sin(angle) * spread;
            drawUnitAt(graphics2D, unit, screenX, screenY, unitRadius, unit.equals(selectedUnit));
        }

        private void drawUnitAt(Graphics2D graphics2D, Unit unit, double screenX, double screenY, boolean highlighted) {
            drawUnitAt(graphics2D, unit, screenX, screenY, currentHexSize() * 0.19, highlighted);
        }

        private void drawUnitAt(Graphics2D graphics2D, Unit unit, double screenX, double screenY, double unitRadius, boolean highlighted) {
            Color unitColor = unitColor(unit.getType());
            graphics2D.setColor(new Color(0, 0, 0, 100));
            graphics2D.fillOval((int) (screenX - unitRadius + 2), (int) (screenY - unitRadius + 3),
                    (int) (unitRadius * 2), (int) (unitRadius * 2));
            graphics2D.setColor(unitColor);
            graphics2D.fillOval((int) (screenX - unitRadius), (int) (screenY - unitRadius),
                    (int) (unitRadius * 2), (int) (unitRadius * 2));
            graphics2D.setColor(highlighted ? Color.WHITE : new Color(25, 30, 36));
            graphics2D.setStroke(new BasicStroke(highlighted ? 3.0f : 1.4f));
            graphics2D.drawOval((int) (screenX - unitRadius), (int) (screenY - unitRadius),
                    (int) (unitRadius * 2), (int) (unitRadius * 2));

            graphics2D.setColor(Color.WHITE);
            graphics2D.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int) (10 * currentZoom()))));
            drawCenteredString(graphics2D, unitShortName(unit.getType()), screenX, screenY + 4);
        }

        private void drawMapInstructions(Graphics2D graphics2D) {
            graphics2D.setColor(new Color(0, 0, 0, 110));
            graphics2D.fillRoundRect(12, 12, 295, 30, 14, 14);
            graphics2D.setColor(new Color(230, 235, 244));
            graphics2D.setFont(new Font("SansSerif", Font.PLAIN, 12));
            graphics2D.drawString("Drag to pan • Mouse wheel zoom • Click hexes", 24, 32);
        }

        private Polygon createHexPolygon(HexCoordinate coordinate) {
            Point2D.Double center = centerOf(coordinate);
            int[] pointXs = new int[6];
            int[] pointYs = new int[6];
            double hexSize = currentHexSize();

            for (int vertexIndex = 0; vertexIndex < 6; vertexIndex++) {
                double angle = Math.toRadians(60 * vertexIndex - 30);
                pointXs[vertexIndex] = (int) Math.round(center.x + hexSize * Math.cos(angle));
                pointYs[vertexIndex] = (int) Math.round(center.y + hexSize * Math.sin(angle));
            }

            return new Polygon(pointXs, pointYs, 6);
        }

        private Point2D.Double centerOf(HexCoordinate coordinate) {
            double hexSize = currentHexSize();
            double screenX = getWidth() / 2.0
                    + panOffsetX
                    + hexSize * ROOT_THREE * (coordinate.getQ() + coordinate.getR() / 2.0);
            double screenY = getHeight() / 2.0
                    + panOffsetY
                    + hexSize * 1.5 * coordinate.getR();
            return new Point2D.Double(screenX, screenY);
        }

        private double currentHexSize() {
            return BASE_HEX_SIZE * currentZoom();
        }

        private double currentZoom() {
            return zoomLevels[zoomIndex];
        }

        private Color terrainColor(Hex hex) {
            Color baseColor = switch (hex.getTerrain()) {
                case TOWN_HALL -> new Color(132, 111, 82);
                case PLAINS -> new Color(204, 179, 106);
                case GRASSLAND -> new Color(99, 165, 94);
                case FOREST -> new Color(59, 126, 76);
                case MOUNTAIN -> new Color(124, 128, 132);
            };

            if (!hex.getAvailableResources().isEmpty()) {
                return brighten(baseColor, 20);
            }
            return baseColor;
        }

        private Color brighten(Color color, int amount) {
            return new Color(
                    Math.min(255, color.getRed() + amount),
                    Math.min(255, color.getGreen() + amount),
                    Math.min(255, color.getBlue() + amount));
        }

        private Color unitColor(UnitType unitType) {
            return switch (unitType) {
                case EXPLORER -> new Color(64, 156, 255);
                case WORKER -> new Color(246, 190, 83);
                case BUILDER -> new Color(126, 212, 116);
                case BORDER_EXPANDER -> new Color(201, 122, 245);
            };
        }

        private String terrainShortName(TerrainType terrainType) {
            return switch (terrainType) {
                case TOWN_HALL -> "Capital";
                case PLAINS -> "Plains";
                case GRASSLAND -> "Grass";
                case FOREST -> "Forest";
                case MOUNTAIN -> "Mount";
            };
        }

        private String resourcesShortName(Set<Resource> resources) {
            List<String> labels = new ArrayList<>();
            for (Resource resource : resources) {
                labels.add(switch (resource) {
                    case WOOD -> "Wood";
                    case STONE -> "Stone";
                    case IRON -> "Iron";
                    case FOOD -> "Food";
                });
            }
            labels.sort(String::compareTo);
            return String.join("/", labels);
        }

        private String buildingShortName(BuildingType buildingType) {
            return switch (buildingType) {
                case TOWN_HALL -> "TH";
                case LUMBER_MILL -> "LM";
                case STONE_MINE -> "SM";
                case IRON_MINE -> "IM";
                case FARM -> "FM";
                case STABLE -> "ST";
                case VILLAGE -> "VG";
                case TOWN -> "TN";
            };
        }

        private String unitShortName(UnitType unitType) {
            return switch (unitType) {
                case EXPLORER -> "E";
                case WORKER -> "W";
                case BUILDER -> "B";
                case BORDER_EXPANDER -> "X";
            };
        }

        private void drawCenteredString(Graphics2D graphics2D, String text, double centerX, double baselineY) {
            FontMetrics fontMetrics = graphics2D.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(text);
            graphics2D.drawString(text, (int) Math.round(centerX - textWidth / 2.0), (int) Math.round(baselineY));
        }
    }

    private final class UnitRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Unit unit) {
                setText(pretty(unit.getType()) + "  AP " + unit.getCurrentAP() + "/" + unit.getType().getEachTurnAP());
            }
            return component;
        }
    }
}
