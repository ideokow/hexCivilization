package view.game;

import model.game.building.BuildingType;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.TerrainType;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

final class HexMapPanel extends JPanel {

    private static final int DRAG_REPAINT_INTERVAL_NS = 16_000_000;
    private static final double ROOT_THREE = 1.7320508075688772;
    private static final int BASE_HEX_SIZE = 32;
    private static final int UNIT_ANIMATION_FRAMES = 1;
    private static final int UNIT_ANIMATION_DELAY_MS = 24;
    private static final int DEFAULT_ZOOM_INDEX = 2;

    private static final double[] ZOOM_LEVELS = {
            0.70,
            0.85,
            1.0,
            1.20,
            1.45,
            1.75,
            2.10
    };

    private final GameViewModel viewModel;
    private final GameViewState viewState;

    private long lastDragRepaintTime;
    private int zoomIndex = DEFAULT_ZOOM_INDEX;
    private int panOffsetX;
    private int panOffsetY;
    private Point lastDragPoint;
    private int dragDistance;

    private Unit movingUnit;
    private HexCoordinate animationOrigin;
    private HexCoordinate animationDestination;
    private int animationFrame;
    private Timer animationTimer;
    private final Deque<MovementAnimation> animationQueue = new ArrayDeque<>();

    HexMapPanel(
            GameViewModel viewModel,
            GameViewState viewState
    ) {
        this.viewModel = viewModel;
        this.viewState = viewState;

        setBackground(new Color(18, 22, 29));
        setFocusable(true);
        configureMouseInteraction();
    }

    private void configureMouseInteraction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                lastDragPoint = event.getPoint();
                dragDistance = 0;
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                handleMouseDragged(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                lastDragPoint = null;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                handleMouseClicked(event);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                int direction =
                        event.getWheelRotation() < 0 ? 1 : -1;

                zoomBy(direction);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private void handleMouseDragged(MouseEvent event) {
        if (lastDragPoint == null) {
            return;
        }

        int deltaX = event.getX() - lastDragPoint.x;
        int deltaY = event.getY() - lastDragPoint.y;

        panOffsetX += deltaX;
        panOffsetY += deltaY;
        dragDistance += Math.abs(deltaX) + Math.abs(deltaY);

        lastDragPoint = event.getPoint();

        long currentTime = System.nanoTime();

        if (currentTime - lastDragRepaintTime
                >= DRAG_REPAINT_INTERVAL_NS) {
            lastDragRepaintTime = currentTime;
            repaint();
        }
    }

    private void handleMouseClicked(MouseEvent event) {
        if (dragDistance > 8) {
            return;
        }

        HexCoordinate clickedCoordinate =
                getCoordinateAt(event.getPoint());

        if (clickedCoordinate != null) {
            viewState.notifyHexClicked(clickedCoordinate);
        }
    }

    void resetCamera() {
        panOffsetX = 0;
        panOffsetY = 0;
        zoomIndex = DEFAULT_ZOOM_INDEX;
        repaint();
    }

    void animateUnitMovement(
            Unit unit,
            HexCoordinate origin,
            HexCoordinate destination
    ) {
        if (unit == null || origin == null || destination == null) {
            return;
        }

        animationQueue.addLast(
                new MovementAnimation(unit, origin, destination)
        );

        if (movingUnit == null) {
            startNextAnimation();
        }
    }

    private void startNextAnimation() {
        MovementAnimation nextAnimation = animationQueue.pollFirst();

        if (nextAnimation == null) {
            stopAnimationTimer();
            return;
        }

        movingUnit = nextAnimation.unit();
        animationOrigin = nextAnimation.origin();
        animationDestination = nextAnimation.destination();
        animationFrame = 0;

        if (animationTimer == null) {
            animationTimer = new Timer(
                    UNIT_ANIMATION_DELAY_MS,
                    event -> advanceAnimation()
            );
        }

        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    private void stopAnimationTimer() {
        if (animationTimer != null
                && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    private void advanceAnimation() {
        animationFrame++;

        if (animationFrame >= UNIT_ANIMATION_FRAMES) {
            movingUnit = null;
            startNextAnimation();
        }

        repaint();
    }

    private void zoomBy(int direction) {
        int nextZoomIndex = Math.max(
                0,
                Math.min(
                        ZOOM_LEVELS.length - 1,
                        zoomIndex + direction
                )
        );

        if (nextZoomIndex != zoomIndex) {
            zoomIndex = nextZoomIndex;
            repaint();
        }
    }

    private HexCoordinate getCoordinateAt(Point point) {
        FractionalHex fractionalHex =
                screenToFractionalHex(
                        point.x,
                        point.y
                );

        HexCoordinate coordinate = roundAxial(
                fractionalHex.q(),
                fractionalHex.r()
        );

        if (!viewModel.containsHex(coordinate)) {
            return null;
        }

        return coordinate;
    }

    private FractionalHex screenToFractionalHex(
            double screenX,
            double screenY
    ) {
        double hexSize = currentHexSize();

        double localX =
                screenX - getWidth() / 2.0 - panOffsetX;
        double localY =
                screenY - getHeight() / 2.0 - panOffsetY;

        double fractionalR =
                (2.0 / 3.0) * localY / hexSize;

        double fractionalQ =
                localX / (ROOT_THREE * hexSize)
                        - fractionalR / 2.0;

        return new FractionalHex(
                fractionalQ,
                fractionalR
        );
    }

    private HexCoordinate roundAxial(
            double q,
            double r
    ) {
        double cubeX = q;
        double cubeZ = r;
        double cubeY = -cubeX - cubeZ;

        int roundedX = (int) Math.round(cubeX);
        int roundedY = (int) Math.round(cubeY);
        int roundedZ = (int) Math.round(cubeZ);

        double xDifference =
                Math.abs(roundedX - cubeX);
        double yDifference =
                Math.abs(roundedY - cubeY);
        double zDifference =
                Math.abs(roundedZ - cubeZ);

        if (xDifference > yDifference
                && xDifference > zDifference) {
            roundedX = -roundedY - roundedZ;
        } else if (yDifference > zDifference) {
            roundedY = -roundedX - roundedZ;
        } else {
            roundedZ = -roundedX - roundedY;
        }

        return new HexCoordinate(roundedX, roundedZ);
    }

    private List<Hex> getVisibleHexes() {
        double margin = currentHexSize() * 2.0;

        FractionalHex topLeft =
                screenToFractionalHex(-margin, -margin);

        FractionalHex topRight =
                screenToFractionalHex(
                        getWidth() + margin,
                        -margin
                );

        FractionalHex bottomLeft =
                screenToFractionalHex(
                        -margin,
                        getHeight() + margin
                );

        FractionalHex bottomRight =
                screenToFractionalHex(
                        getWidth() + margin,
                        getHeight() + margin
                );

        double minQValue = Math.min(
                Math.min(topLeft.q(), topRight.q()),
                Math.min(bottomLeft.q(), bottomRight.q())
        );

        double maxQValue = Math.max(
                Math.max(topLeft.q(), topRight.q()),
                Math.max(bottomLeft.q(), bottomRight.q())
        );

        double minRValue = Math.min(
                Math.min(topLeft.r(), topRight.r()),
                Math.min(bottomLeft.r(), bottomRight.r())
        );

        double maxRValue = Math.max(
                Math.max(topLeft.r(), topRight.r()),
                Math.max(bottomLeft.r(), bottomRight.r())
        );

        int minQ = (int) Math.floor(minQValue) - 1;
        int maxQ = (int) Math.ceil(maxQValue) + 1;
        int minR = (int) Math.floor(minRValue) - 1;
        int maxR = (int) Math.ceil(maxRValue) + 1;

        return viewModel.getHexesInBounds(
                minQ,
                maxQ,
                minR,
                maxR
        );
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D =
                (Graphics2D) graphics.create();

        try {
            configureRendering(graphics2D);
            drawBackground(graphics2D);

            List<Hex> visibleHexes = getVisibleHexes();

            drawHexes(graphics2D, visibleHexes);
            drawUnits(graphics2D, visibleHexes);
            drawMovingUnit(graphics2D);
            drawMapInstructions(graphics2D);
        } finally {
            graphics2D.dispose();
        }
    }

    private void configureRendering(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    private void drawBackground(Graphics2D graphics2D) {
        graphics2D.setPaint(
                new GradientPaint(
                        0,
                        0,
                        new Color(24, 31, 43),
                        0,
                        getHeight(),
                        new Color(12, 15, 21)
                )
        );

        graphics2D.fillRect(
                0,
                0,
                getWidth(),
                getHeight()
        );
    }

    private void drawHexes(
            Graphics2D graphics2D,
            List<Hex> visibleHexes
    ) {
        for (Hex hex : visibleHexes) {
            drawHex(graphics2D, hex);
        }
    }

    private void drawHex(
            Graphics2D graphics2D,
            Hex hex
    ) {
        HexCoordinate coordinate = hex.getCoordinate();
        Polygon polygon = createHexPolygon(coordinate);
        boolean discovered =
                viewModel.isDiscovered(coordinate);

        Color fillColor = discovered
                ? terrainColor(hex)
                : new Color(29, 33, 43);

        graphics2D.setColor(fillColor);
        graphics2D.fillPolygon(polygon);

        drawHexBorder(
                graphics2D,
                coordinate,
                polygon,
                discovered
        );

        if (coordinate.equals(viewState.getSelectedHex())) {
            drawSelectionBorder(graphics2D, polygon);
        }

        if (discovered) {
            drawHexContent(graphics2D, hex);
        } else {
            drawFogMark(graphics2D, polygon);
        }
    }

    private void drawHexBorder(
            Graphics2D graphics2D,
            HexCoordinate coordinate,
            Polygon polygon,
            boolean discovered
    ) {
        if (viewModel.ownsTerritory(coordinate)) {
            graphics2D.setColor(
                    new Color(231, 184, 77)
            );
            graphics2D.setStroke(new BasicStroke(2.2f));
        } else {
            graphics2D.setColor(
                    discovered
                            ? new Color(70, 80, 92)
                            : new Color(43, 48, 58)
            );
            graphics2D.setStroke(new BasicStroke(1.0f));
        }

        graphics2D.drawPolygon(polygon);
    }

    private void drawSelectionBorder(
            Graphics2D graphics2D,
            Polygon polygon
    ) {
        graphics2D.setColor(
                new Color(255, 255, 255, 180)
        );
        graphics2D.setStroke(new BasicStroke(3.0f));
        graphics2D.drawPolygon(polygon);
    }

    private void drawHexContent(
            Graphics2D graphics2D,
            Hex hex
    ) {
        Point2D.Double center =
                centerOf(hex.getCoordinate());
        double hexSize = currentHexSize();

        drawTerrainLabel(
                graphics2D,
                hex,
                center,
                hexSize
        );

        if (!hex.getAvailableResources().isEmpty()) {
            drawResources(
                    graphics2D,
                    hex.getAvailableResources(),
                    center,
                    hexSize
            );
        }

        if (hex.getBuilding() != null) {
            drawBuilding(
                    graphics2D,
                    hex.getBuilding().getType(),
                    center,
                    hexSize
            );
        }
    }

    private void drawTerrainLabel(
            Graphics2D graphics2D,
            Hex hex,
            Point2D.Double center,
            double hexSize
    ) {
        graphics2D.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        Math.max(
                                10,
                                (int) (11 * currentZoom())
                        )
                )
        );

        graphics2D.setColor(
                new Color(25, 30, 37, 180)
        );

        drawCenteredString(
                graphics2D,
                terrainShortName(hex.getTerrain()),
                center.x,
                center.y - hexSize * 0.15
        );
    }

    private void drawResources(
            Graphics2D graphics2D,
            Set<Resource> resources,
            Point2D.Double center,
            double hexSize
    ) {
        graphics2D.setColor(
                new Color(255, 255, 255, 215)
        );

        graphics2D.fillRoundRect(
                (int) (center.x - hexSize * 0.48),
                (int) (center.y + hexSize * 0.12),
                (int) (hexSize * 0.96),
                (int) (hexSize * 0.34),
                12,
                12
        );

        graphics2D.setColor(new Color(54, 58, 64));
        graphics2D.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        Math.max(
                                9,
                                (int) (10 * currentZoom())
                        )
                )
        );

        drawCenteredString(
                graphics2D,
                resourcesShortName(resources),
                center.x,
                center.y + hexSize * 0.36
        );
    }

    private void drawBuilding(
            Graphics2D graphics2D,
            BuildingType buildingType,
            Point2D.Double center,
            double hexSize
    ) {
        graphics2D.setColor(
                new Color(30, 34, 42, 220)
        );

        graphics2D.fillOval(
                (int) (center.x - hexSize * 0.28),
                (int) (center.y - hexSize * 0.92),
                (int) (hexSize * 0.56),
                (int) (hexSize * 0.36)
        );

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        Math.max(
                                9,
                                (int) (10 * currentZoom())
                        )
                )
        );

        drawCenteredString(
                graphics2D,
                buildingShortName(buildingType),
                center.x,
                center.y - hexSize * 0.66
        );
    }

    private void drawFogMark(
            Graphics2D graphics2D,
            Polygon polygon
    ) {
        Rectangle bounds = polygon.getBounds();

        graphics2D.setColor(
                new Color(7, 9, 13, 95)
        );
        graphics2D.fillPolygon(polygon);

        graphics2D.setColor(
                new Color(95, 103, 118, 90)
        );
        graphics2D.setFont(
                new Font("SansSerif", Font.BOLD, 14)
        );

        drawCenteredString(
                graphics2D,
                "?",
                bounds.getCenterX(),
                bounds.getCenterY() + 5
        );
    }

    private void drawUnits(
            Graphics2D graphics2D,
            List<Hex> visibleHexes
    ) {
        for (Hex hex : visibleHexes) {
            HexCoordinate coordinate = hex.getCoordinate();

            if (!viewModel.isDiscovered(coordinate)) {
                continue;
            }

            List<Unit> units = hex.getUnits();

            for (
                    int unitIndex = 0;
                    unitIndex < units.size();
                    unitIndex++
            ) {
                Unit unit = units.get(unitIndex);

                if (isAnimationPending(unit)) {
                    continue;
                }

                drawUnit(
                        graphics2D,
                        unit,
                        coordinate,
                        unitIndex,
                        units.size()
                );
            }
        }
    }

    private boolean isAnimationPending(Unit unit) {
        if (unit.equals(movingUnit)) {
            return true;
        }

        for (MovementAnimation animation : animationQueue) {
            if (unit.equals(animation.unit())) {
                return true;
            }
        }

        return false;
    }

    private void drawMovingUnit(Graphics2D graphics2D) {
        if (movingUnit == null
                || animationOrigin == null
                || animationDestination == null) {
            return;
        }

        Point2D.Double originCenter =
                centerOf(animationOrigin);

        Point2D.Double destinationCenter =
                centerOf(animationDestination);

        double progress = Math.min(
                1.0,
                animationFrame
                        / (double) UNIT_ANIMATION_FRAMES
        );

        double smoothProgress =
                progress
                        * progress
                        * (3.0 - 2.0 * progress);

        double screenX = originCenter.x
                + (destinationCenter.x - originCenter.x)
                * smoothProgress;

        double screenY = originCenter.y
                + (destinationCenter.y - originCenter.y)
                * smoothProgress;

        drawUnitAt(
                graphics2D,
                movingUnit,
                screenX,
                screenY,
                true
        );
    }

    private void drawUnit(
            Graphics2D graphics2D,
            Unit unit,
            HexCoordinate coordinate,
            int unitIndex,
            int unitsInHex
    ) {
        Point2D.Double center = centerOf(coordinate);
        double unitRadius = currentHexSize() * 0.19;

        double angle =
                (Math.PI * 2.0 * unitIndex)
                        / Math.max(1, unitsInHex);

        double spread = unitsInHex == 1
                ? 0
                : currentHexSize() * 0.33;

        double screenX =
                center.x + Math.cos(angle) * spread;
        double screenY =
                center.y + Math.sin(angle) * spread;

        drawUnitAt(
                graphics2D,
                unit,
                screenX,
                screenY,
                unitRadius,
                unit.equals(viewState.getSelectedUnit())
        );
    }

    private void drawUnitAt(
            Graphics2D graphics2D,
            Unit unit,
            double screenX,
            double screenY,
            boolean highlighted
    ) {
        drawUnitAt(
                graphics2D,
                unit,
                screenX,
                screenY,
                currentHexSize() * 0.19,
                highlighted
        );
    }

    private void drawUnitAt(
            Graphics2D graphics2D,
            Unit unit,
            double screenX,
            double screenY,
            double unitRadius,
            boolean highlighted
    ) {
        drawUnitShadow(
                graphics2D,
                screenX,
                screenY,
                unitRadius
        );

        graphics2D.setColor(unitColor(unit.getType()));
        graphics2D.fillOval(
                (int) (screenX - unitRadius),
                (int) (screenY - unitRadius),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );

        graphics2D.setColor(
                highlighted
                        ? Color.WHITE
                        : new Color(25, 30, 36)
        );

        graphics2D.setStroke(
                new BasicStroke(highlighted ? 3.0f : 1.4f)
        );

        graphics2D.drawOval(
                (int) (screenX - unitRadius),
                (int) (screenY - unitRadius),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        Math.max(
                                9,
                                (int) (10 * currentZoom())
                        )
                )
        );

        drawCenteredString(
                graphics2D,
                unitShortName(unit.getType()),
                screenX,
                screenY + 4
        );
    }

    private void drawUnitShadow(
            Graphics2D graphics2D,
            double screenX,
            double screenY,
            double unitRadius
    ) {
        graphics2D.setColor(new Color(0, 0, 0, 100));

        graphics2D.fillOval(
                (int) (screenX - unitRadius + 2),
                (int) (screenY - unitRadius + 3),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );
    }

    private void drawMapInstructions(
            Graphics2D graphics2D
    ) {
        graphics2D.setColor(new Color(0, 0, 0, 110));
        graphics2D.fillRoundRect(
                12,
                12,
                295,
                30,
                14,
                14
        );

        graphics2D.setColor(
                new Color(230, 235, 244)
        );
        graphics2D.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );

        graphics2D.drawString(
                "Drag to pan • Mouse wheel zoom • Click hexes",
                24,
                32
        );
    }

    private Polygon createHexPolygon(
            HexCoordinate coordinate
    ) {
        Point2D.Double center = centerOf(coordinate);
        int[] pointXs = new int[6];
        int[] pointYs = new int[6];
        double hexSize = currentHexSize();

        for (
                int vertexIndex = 0;
                vertexIndex < 6;
                vertexIndex++
        ) {
            double angle = Math.toRadians(
                    60 * vertexIndex - 30
            );

            pointXs[vertexIndex] = (int) Math.round(
                    center.x + hexSize * Math.cos(angle)
            );

            pointYs[vertexIndex] = (int) Math.round(
                    center.y + hexSize * Math.sin(angle)
            );
        }

        return new Polygon(pointXs, pointYs, 6);
    }

    private Point2D.Double centerOf(
            HexCoordinate coordinate
    ) {
        double hexSize = currentHexSize();

        double screenX =
                getWidth() / 2.0
                        + panOffsetX
                        + hexSize
                        * ROOT_THREE
                        * (
                        coordinate.getQ()
                                + coordinate.getR() / 2.0
                );

        double screenY =
                getHeight() / 2.0
                        + panOffsetY
                        + hexSize
                        * 1.5
                        * coordinate.getR();

        return new Point2D.Double(screenX, screenY);
    }

    private double currentHexSize() {
        return BASE_HEX_SIZE * currentZoom();
    }

    private double currentZoom() {
        return ZOOM_LEVELS[zoomIndex];
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

    private Color brighten(
            Color color,
            int amount
    ) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount)
        );
    }

    private Color unitColor(UnitType unitType) {
        return switch (unitType) {
            case EXPLORER -> new Color(64, 156, 255);
            case WORKER -> new Color(246, 190, 83);
            case BUILDER -> new Color(126, 212, 116);
            case BORDER_EXPANDER ->
                    new Color(201, 122, 245);
        };
    }

    private String terrainShortName(
            TerrainType terrainType
    ) {
        return switch (terrainType) {
            case TOWN_HALL -> "Capital";
            case PLAINS -> "Plains";
            case GRASSLAND -> "Grass";
            case FOREST -> "Forest";
            case MOUNTAIN -> "Mount";
        };
    }

    private String resourcesShortName(
            Set<Resource> resources
    ) {
        List<String> labels = new ArrayList<>();

        for (Resource resource : resources) {
            labels.add(
                    switch (resource) {
                        case WOOD -> "Wood";
                        case STONE -> "Stone";
                        case IRON -> "Iron";
                        case FOOD -> "Food";
                    }
            );
        }

        labels.sort(String::compareTo);
        return String.join("/", labels);
    }

    private String buildingShortName(
            BuildingType buildingType
    ) {
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

    private void drawCenteredString(
            Graphics2D graphics2D,
            String text,
            double centerX,
            double baselineY
    ) {
        FontMetrics fontMetrics =
                graphics2D.getFontMetrics();

        int textWidth =
                fontMetrics.stringWidth(text);

        graphics2D.drawString(
                text,
                (int) Math.round(
                        centerX - textWidth / 2.0
                ),
                (int) Math.round(baselineY)
        );
    }

    private record FractionalHex(double q, double r) {
    }

    private record MovementAnimation(
            Unit unit,
            HexCoordinate origin,
            HexCoordinate destination
    ) {
    }
}
