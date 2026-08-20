package view.game;

import model.game.happiness.Era;
import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.unit.Unit;
import model.game.unit.UnitType;

import javax.swing.*;
import java.util.Map;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HexMapPanel extends JPanel {

    private static final int DRAG_REPAINT_INTERVAL_NS = 16_000_000;
    private static final double ROOT_THREE = 1.7320508075688772;
    private static final int BASE_HEX_SIZE = 32;
    private static final int UNIT_ANIMATION_FRAMES = 1;
    private static final int UNIT_ANIMATION_DELAY_MS = 24;

    // continuous zoom: see much further out or in without steps
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 2.50;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double ZOOM_STEP_FACTOR = 1.12;
    private static final double LOW_DETAIL_ZOOM = 0.50;
    private static final double UNIT_LABEL_ZOOM = 0.70;

    private static final Color BACKGROUND_TOP = new Color(24, 31, 43);
    private static final Color BACKGROUND_BOTTOM = new Color(12, 15, 21);
    private static final Color UNIT_TEXT = new Color(25, 30, 36);
    private static final Color UNIT_SHADOW = new Color(0, 0, 0, 100);
    private static final Color INSTRUCTION_BACKGROUND = new Color(0, 0, 0, 110);
    private static final Color INSTRUCTION_TEXT = new Color(230, 235, 244);
    private static final Color TERRITORY_BORDER = new Color(231, 184, 77);
    private static final Color SELECTION_FILL = new Color(235, 245, 255, 34);
    private static final Color SELECTION_BORDER = new Color(235, 245, 255, 190);
    private static final Color EXPLORER_COLOR = new Color(64, 156, 255);
    private static final Color WORKER_COLOR = new Color(246, 190, 83);
    private static final Color BUILDER_COLOR = new Color(126, 212, 116);
    private static final Color BORDER_EXPANDER_COLOR = new Color(201, 122, 245);
    private static final Color PLAYER_SWORDSMAN_COLOR = new Color(69, 121, 214);
    private static final Color PLAYER_ARCHER_COLOR = new Color(54, 157, 132);
    private static final Color PLAYER_CAVALRY_COLOR = new Color(183, 123, 52);
    private static final Color TRIBE_SWORDSMAN_COLOR = new Color(191, 75, 83);
    private static final Color TRIBE_ARCHER_COLOR = new Color(176, 77, 139);
    private static final Color TRIBE_CAVALRY_COLOR = new Color(164, 82, 52);

    private static final BasicStroke HEX_STROKE = new BasicStroke(1.0f);
    private static final BasicStroke TERRITORY_STROKE = new BasicStroke(2.2f);
    private static final BasicStroke SELECTION_STROKE = new BasicStroke(1.5f);
    private static final BasicStroke UNIT_STROKE = new BasicStroke(1.4f);
    private static final BasicStroke HIGHLIGHTED_UNIT_STROKE = new BasicStroke(3.0f);

    private static final Font INSTRUCTION_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font INFO_FONT = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font UNIT_FONT = new Font("SansSerif", Font.BOLD, 10);

    private static final double[] HEX_COSINES = new double[6];
    private static final double[] HEX_SINES = new double[6];

    static {
        for (int vertexIndex = 0; vertexIndex < 6; vertexIndex++) {
            double angle = Math.toRadians(60 * vertexIndex - 30);
            HEX_COSINES[vertexIndex] = Math.cos(angle);
            HEX_SINES[vertexIndex] = Math.sin(angle);
        }
    }

    private final GameViewModel viewModel;
    private final GameViewState viewState;
    private final HexTileRenderer tileRenderer = new HexTileRenderer();

    private long lastDragRepaintTime;
    private double zoomFactor = DEFAULT_ZOOM;
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
    private final Set<Unit> animationPendingUnits = new HashSet<>();

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

                zoomBy(direction, event.getPoint());
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
        zoomFactor = DEFAULT_ZOOM;
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

        animationQueue.addLast(new MovementAnimation(unit, origin, destination));
        animationPendingUnits.add(unit);

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
            Unit completedUnit = movingUnit;
            movingUnit = null;
            if (completedUnit != null && !hasQueuedAnimation(completedUnit)) {
                animationPendingUnits.remove(completedUnit);
            }
            startNextAnimation();
        }

        repaint();
    }

    // smooth zoom anchored at the cursor position
    private void zoomBy(int direction, Point anchor) {
        double nextZoom = Math.max(
                MIN_ZOOM,
                Math.min(
                        MAX_ZOOM,
                        zoomFactor * Math.pow(ZOOM_STEP_FACTOR, direction)
                )
        );

        if (nextZoom == zoomFactor) {
            return;
        }

        double centerX = getWidth() / 2.0 + panOffsetX;
        double centerY = getHeight() / 2.0 + panOffsetY;
        double ratio = nextZoom / zoomFactor;

        panOffsetX = (int) Math.round(anchor.x - getWidth() / 2.0 - (anchor.x - centerX) * ratio);
        panOffsetY = (int) Math.round(anchor.y - getHeight() / 2.0 - (anchor.y - centerY) * ratio);
        zoomFactor = nextZoom;
        repaint();
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
        // One hex radius is enough to include cells whose polygon touches
        // the panel edge; larger margins create many off-screen candidates.
        double margin = currentHexSize();

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

        List<Hex> candidates = viewModel.getHexesInBounds(
                minQ,
                maxQ,
                minR,
                maxR
        );

        List<Hex> visibleHexes = new ArrayList<>();
        double hexSize = currentHexSize();

        for (Hex hex : candidates) {
            if (intersectsPanel(hex.getCoordinate(), hexSize)) {
                visibleHexes.add(hex);
            }
        }

        return visibleHexes;
    }

    private boolean intersectsPanel(
            HexCoordinate coordinate,
            double hexSize
    ) {
        double centerX = getWidth() / 2.0
                + panOffsetX
                + hexSize
                * ROOT_THREE
                * (coordinate.getQ() + coordinate.getR() / 2.0);

        double centerY = getHeight() / 2.0
                + panOffsetY
                + hexSize * 1.5 * coordinate.getR();

        // A flat-top hexagon fits inside this square. The test deliberately
        // keeps any hex whose visual bounds touch the panel edge.
        return centerX + hexSize >= 0
                && centerX - hexSize <= getWidth()
                && centerY + hexSize >= 0
                && centerY - hexSize <= getHeight();
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
            drawTopLeftInfoBox(graphics2D);
        } finally {
            graphics2D.dispose();
        }
    }

    private void configureRendering(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED
        );

        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                zoomFactor < LOW_DETAIL_ZOOM
                        ? RenderingHints.VALUE_ANTIALIAS_OFF
                        : RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                zoomFactor < LOW_DETAIL_ZOOM
                        ? RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
                        : RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    private void drawBackground(Graphics2D graphics2D) {
        graphics2D.setPaint(
                new GradientPaint(
                        0,
                        0,
                        BACKGROUND_TOP,
                        0,
                        getHeight(),
                        BACKGROUND_BOTTOM
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
        boolean discovered =
                viewModel.isDiscovered(coordinate);

        Set<Resource> resources = discovered && zoomFactor >= LOW_DETAIL_ZOOM
                ? hex.getAvailableResources()
                : Set.of();
        Point2D.Double center = centerOf(coordinate);
        if (zoomFactor < LOW_DETAIL_ZOOM) {
            tileRenderer.drawLowDetail(
                    graphics2D,
                    hex,
                    discovered,
                    resources,
                    center.x,
                    center.y,
                    currentHexSize()
            );
        } else {
            HexTileRenderer.CachedSprite cached = tileRenderer.spriteFor(
                    hex,
                    discovered,
                    resources,
                    zoomFactor
            );
            BufferedImage tileSprite = cached.image();

            // scale the sprite (rendered at bucketZoom) to the current zoom factor
            double spriteScale = zoomFactor / (2.0 * cached.bucketZoom());
            int spriteWidth = (int) Math.round(tileSprite.getWidth() * spriteScale);
            int spriteHeight = (int) Math.round(tileSprite.getHeight() * spriteScale);

            graphics2D.drawImage(
                    tileSprite,
                    (int) Math.round(center.x - spriteWidth / 2.0),
                    (int) Math.round(center.y - spriteHeight / 2.0),
                    spriteWidth,
                    spriteHeight,
                    null
            );
        }

        Polygon polygon = null;
        if (zoomFactor >= LOW_DETAIL_ZOOM
                && viewModel.ownsTerritory(coordinate)) {
            polygon = createHexPolygon(coordinate);
            drawTerritoryBorder(graphics2D, polygon);
        }

        if (coordinate.equals(viewState.getSelectedHex())) {
            drawSelectionBorder(graphics2D, coordinate);
        }
    }

    private void drawTerritoryBorder(
            Graphics2D graphics2D,
            Polygon polygon
    ) {
        graphics2D.setColor(TERRITORY_BORDER);
        graphics2D.setStroke(TERRITORY_STROKE);
        graphics2D.drawPolygon(polygon);
    }

    private void drawSelectionBorder(
            Graphics2D graphics2D,
            HexCoordinate coordinate
    ) {
        Point2D.Double center = centerOf(coordinate);
        Polygon selectionPolygon = createHexPolygonAt(
                center.x,
                center.y,
                currentHexSize() * 0.78
        );

        graphics2D.setColor(SELECTION_FILL);
        graphics2D.fillPolygon(selectionPolygon);
        graphics2D.setColor(SELECTION_BORDER);
        graphics2D.setStroke(SELECTION_STROKE);
        graphics2D.drawPolygon(selectionPolygon);
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
        return animationPendingUnits.contains(unit);
    }

    private boolean hasQueuedAnimation(Unit unit) {
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

        graphics2D.setColor(unitColor(unit));
        graphics2D.fillOval(
                (int) (screenX - unitRadius),
                (int) (screenY - unitRadius),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );

        graphics2D.setColor(highlighted ? Color.WHITE : UNIT_TEXT);
        graphics2D.setStroke(highlighted ? HIGHLIGHTED_UNIT_STROKE : UNIT_STROKE);

        graphics2D.drawOval(
                (int) (screenX - unitRadius),
                (int) (screenY - unitRadius),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );

        if (zoomFactor >= UNIT_LABEL_ZOOM) {
            graphics2D.setColor(Color.WHITE);
            graphics2D.setFont(
                    UNIT_FONT.deriveFont((float) Math.max(7, 10 * zoomFactor))
            );

            drawCenteredString(
                    graphics2D,
                    unitShortName(unit.getType()),
                    screenX,
                    screenY + 4
            );
        }
    }

    private void drawUnitShadow(
            Graphics2D graphics2D,
            double screenX,
            double screenY,
            double unitRadius
    ) {
        graphics2D.setColor(UNIT_SHADOW);

        graphics2D.fillOval(
                (int) (screenX - unitRadius + 2),
                (int) (screenY - unitRadius + 3),
                (int) (unitRadius * 2),
                (int) (unitRadius * 2)
        );
    }

    private void drawTopLeftInfoBox(Graphics2D graphics2D) {
        TownHall townHall = viewModel.getTownHall();

        // -- fonts --
        Font normalFont = new Font("SansSerif", Font.PLAIN, 13);
        Font boldFont = new Font("SansSerif", Font.BOLD, 13);
        FontMetrics normalMetrics = graphics2D.getFontMetrics(normalFont);
        int lineHeight = normalMetrics.getHeight();
        int lineGap = 2;

        // -- build content lines --
        // Section 1: Era & Happiness
        Era era = townHall.getHappiness().getEra();
        String[] ERA_EMOJIS = {
                "\uD83D\uDC4D",  // Golden Age
                "\uD83D\uDE10",  // Normal
                "\uD83D\uDE1E",  // Discontent
                "\uD83D\uDD25"   // Rebellion
        };
        String eraText = ERA_EMOJIS[era.ordinal()] + " " + ViewTextFormatter.pretty(era);
        String happinessText = "Happiness " + townHall.getHappiness().getValue();
        String townHallLevelText =
                "Town Hall Level " + townHall.getLevel().getLevelN();

        // Section 2: Units
        String unitsText = "Units " + townHall.getUnitNumber() + "/" + townHall.getUnitCap();


        int militaryCount = 0;
        for (Unit unit : viewModel.getUnits()) {
            if (UnitType.isMilitary(unit.getType())) {
                militaryCount++;
            }
        }
        String militaryText = "Military " + militaryCount + "/" + townHall.getMilitaryUnitCap();

        // Section 3: Resources
        Resource[] storedResources = {
                Resource.FOOD,
                Resource.WOOD,
                Resource.STONE,
                Resource.IRON
        };
        Map<Resource, Integer> storage = townHall.getResourceStorage();
        String[] resourceLines = new String[4];
        for (int i = 0; i < 4; i++) {
            Resource r = storedResources[i];
            int amount = storage.getOrDefault(r, 0);
            int net = viewModel.getNetResource(r);
            String netStr = (net >= 0 ? "+" : "") + net;
            resourceLines[i] = r.getDisplayName() + " " + amount + " (" + netStr + "/turn)";
        }

        // -- layout calculations --
        int sepGap = 7;
        int paddingX = 12;
        int paddingY = 10;
        int boxX = 12;
        int boxY = 12;

        // measure each section's width using the appropriate font
        int sec1w = Math.max(
                graphics2D.getFontMetrics(boldFont).stringWidth(eraText),
                Math.max(
                        graphics2D.getFontMetrics(normalFont).stringWidth(happinessText),
                        graphics2D.getFontMetrics(boldFont).stringWidth(townHallLevelText)
                )
        );
        int sec2w = Math.max(
                graphics2D.getFontMetrics(boldFont).stringWidth(unitsText),
                graphics2D.getFontMetrics(normalFont).stringWidth(militaryText)
        );
        int sec3w = 0;
        for (String rl : resourceLines) {
            int w = graphics2D.getFontMetrics(normalFont).stringWidth(rl);
            if (w > sec3w) sec3w = w;
        }

        int maxWidth = Math.max(sec1w, Math.max(sec2w, sec3w));
        int boxWidth = maxWidth + paddingX * 2 + 10;

        int dividerPadding = 4;
        int rowAdvance = lineHeight + lineGap;
        int dividerAdvance = normalMetrics.getDescent()
                + dividerPadding * 2
                + normalMetrics.getAscent();
        int baselineSpan = rowAdvance * 2
                + dividerAdvance
                + rowAdvance
                + dividerAdvance
                + rowAdvance * 3;
        int boxHeight = paddingY * 2
                + normalMetrics.getAscent()
                + baselineSpan
                + normalMetrics.getDescent();

        // -- draw box background --
        graphics2D.setColor(new Color(10, 12, 18, 230));
        graphics2D.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);

        // -- draw box border --
        graphics2D.setColor(new Color(180, 190, 210, 200));
        graphics2D.setStroke(new BasicStroke(1.2f));
        graphics2D.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 12, 12);

        // -- draw text and separators --
        graphics2D.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        int textX = boxX + paddingX;
        int textY = boxY + paddingY + normalMetrics.getAscent();

        // Section 1: Era + Happiness
        graphics2D.setFont(boldFont);
        graphics2D.setColor(INSTRUCTION_TEXT);
        graphics2D.drawString(eraText, textX, textY);
        textY += rowAdvance;
        graphics2D.setFont(normalFont);
        graphics2D.drawString(happinessText, textX, textY);
        textY += rowAdvance;
        graphics2D.setFont(boldFont);
        graphics2D.drawString(townHallLevelText, textX, textY);

        int dividerY = textY + normalMetrics.getDescent() + dividerPadding;
        graphics2D.setColor(new Color(160, 175, 200, 100));
        graphics2D.drawLine(
                boxX + paddingX,
                dividerY,
                boxX + boxWidth - paddingX,
                dividerY
        );
        textY = dividerY + dividerPadding + normalMetrics.getAscent();

        graphics2D.setFont(boldFont);
        graphics2D.setColor(INSTRUCTION_TEXT);
        graphics2D.drawString(unitsText, textX, textY);
        textY += rowAdvance;
        graphics2D.setFont(normalFont);
        graphics2D.drawString(militaryText, textX, textY);

        dividerY = textY + normalMetrics.getDescent() + dividerPadding;
        graphics2D.setColor(new Color(160, 175, 200, 100));
        graphics2D.drawLine(
                boxX + paddingX,
                dividerY,
                boxX + boxWidth - paddingX,
                dividerY
        );
        textY = dividerY + dividerPadding + normalMetrics.getAscent();

        graphics2D.setFont(normalFont);
        for (String rl : resourceLines) {
            graphics2D.setColor(INSTRUCTION_TEXT);
            graphics2D.drawString(rl, textX, textY);
            textY += rowAdvance;
        }
    }

    private Polygon createHexPolygon(
            HexCoordinate coordinate
    ) {
        Point2D.Double center = centerOf(coordinate);
        return createHexPolygonAt(center.x, center.y, currentHexSize());
    }

    private Polygon createHexPolygonAt(
            double centerX,
            double centerY,
            double hexSize
    ) {
        int[] pointXs = new int[6];
        int[] pointYs = new int[6];

        for (
                int vertexIndex = 0;
                vertexIndex < 6;
                vertexIndex++
        ) {
            pointXs[vertexIndex] = (int) Math.round(
                    centerX + hexSize * HEX_COSINES[vertexIndex]
            );

            pointYs[vertexIndex] = (int) Math.round(
                    centerY + hexSize * HEX_SINES[vertexIndex]
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
        return BASE_HEX_SIZE * zoomFactor;
    }

    private Color unitColor(Unit unit) {
        return switch (unit.getType()) {
            case EXPLORER -> EXPLORER_COLOR;
            case WORKER -> WORKER_COLOR;
            case BUILDER -> BUILDER_COLOR;
            case BORDER_EXPANDER -> BORDER_EXPANDER_COLOR;
            case SWORDSMAN -> unit.isOwnedByPlayer()
                    ? PLAYER_SWORDSMAN_COLOR
                    : TRIBE_SWORDSMAN_COLOR;
            case ARCHER -> unit.isOwnedByPlayer()
                    ? PLAYER_ARCHER_COLOR
                    : TRIBE_ARCHER_COLOR;
            case CAVALRY -> unit.isOwnedByPlayer()
                    ? PLAYER_CAVALRY_COLOR
                    : TRIBE_CAVALRY_COLOR;
        };
    }

    private String unitShortName(UnitType unitType) {
        return switch (unitType) {
            case EXPLORER -> "E";
            case WORKER -> "W";
            case BUILDER -> "B";
            case BORDER_EXPANDER -> "X";
            case SWORDSMAN -> "S";
            case ARCHER -> "A";
            case CAVALRY -> "C";
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
