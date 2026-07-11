package view.game;

import model.game.building.BuildingType;
import model.game.hex.Hex;
import model.game.hex.Resource;
import model.game.hex.TerrainType;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Renders and caches the static visual contents of one map tile. */
final class HexTileRenderer {

    private static final int BASE_HEX_SIZE = 32;
    private static final double[] ZOOM_LEVELS = {
            0.70, 0.85, 1.0, 1.20, 1.45, 1.75, 2.10
    };

    private static final Color FOG_FILL = new Color(29, 33, 43);
    private static final Color FOG_OVERLAY = new Color(7, 9, 13, 95);
    private static final Color FOG_TEXT = new Color(95, 103, 118, 90);
    private static final Color TERRAIN_TEXT = new Color(25, 30, 37, 180);
    private static final Color RESOURCE_BACKGROUND = new Color(255, 255, 255, 215);
    private static final Color RESOURCE_TEXT = new Color(54, 58, 64);
    private static final Color BUILDING_BACKGROUND = new Color(30, 34, 42, 220);
    private static final Color HEX_BORDER = new Color(70, 80, 92);
    private static final Color FOG_BORDER = new Color(43, 48, 58);

    private static final BasicStroke HEX_STROKE = new BasicStroke(1.0f);
    private static final Font FOG_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font[] TERRAIN_FONTS = createFonts(Font.BOLD, 11, 10);
    private static final Font[] SMALL_BOLD_FONTS = createFonts(Font.BOLD, 10, 9);
    private static final double[] HEX_COSINES = new double[6];
    private static final double[] HEX_SINES = new double[6];
    private static final Map<TerrainType, Color> TERRAIN_COLORS = createTerrainColors();
    private static final Map<TerrainType, Color> RESOURCE_TERRAIN_COLORS =
            createResourceTerrainColors();

    static {
        for (int index = 0; index < 6; index++) {
            double angle = Math.toRadians(60 * index - 30);
            HEX_COSINES[index] = Math.cos(angle);
            HEX_SINES[index] = Math.sin(angle);
        }
    }

    private final Map<Integer, Map<TileKey, BufferedImage>> spriteCaches = new HashMap<>();
    private final Map<Integer, String> resourceLabels = new HashMap<>();

    BufferedImage spriteFor(
            Hex hex,
            boolean discovered,
            Set<Resource> resources,
            int zoomIndex
    ) {
        TileKey key = new TileKey(
                discovered,
                hex.getTerrain(),
                resourceMask(resources),
                hex.getBuilding() == null ? null : hex.getBuilding().getType()
        );

        Map<TileKey, BufferedImage> cache = spriteCaches.computeIfAbsent(
                zoomIndex,
                ignored -> new HashMap<>()
        );
        return cache.computeIfAbsent(
                key,
                ignored -> createTileSprite(key, zoomIndex)
        );
    }

    private BufferedImage createTileSprite(TileKey key, int zoomIndex) {
        double hexSize = BASE_HEX_SIZE * ZOOM_LEVELS[zoomIndex];
        int imageSize = (int) Math.ceil(hexSize * 2.0) + 4;
        BufferedImage image = new BufferedImage(
                imageSize,
                imageSize,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = image.createGraphics();
        try {
            configureRendering(graphics);
            double center = imageSize / 2.0;
            Polygon polygon = createHexPolygon(center, center, hexSize);
            Set<Resource> resources = resourcesFromMask(key.resourceMask());

            graphics.setColor(key.discovered()
                    ? terrainColor(key.terrain(), resources)
                    : FOG_FILL);
            graphics.fillPolygon(polygon);
            drawBorder(graphics, polygon, key.discovered());

            if (key.discovered()) {
                drawContent(
                        graphics,
                        key,
                        resources,
                        center,
                        hexSize,
                        zoomIndex
                );
            } else {
                drawFogMark(graphics, polygon);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void drawContent(
            Graphics2D graphics,
            TileKey key,
            Set<Resource> resources,
            double center,
            double hexSize,
            int zoomIndex
    ) {
        graphics.setFont(TERRAIN_FONTS[zoomIndex]);
        graphics.setColor(TERRAIN_TEXT);
        drawCenteredString(
                graphics,
                terrainShortName(key.terrain()),
                center,
                center - hexSize * 0.15
        );

        if (!resources.isEmpty()) {
            graphics.setColor(RESOURCE_BACKGROUND);
            graphics.fillRoundRect(
                    (int) (center - hexSize * 0.48),
                    (int) (center + hexSize * 0.12),
                    (int) (hexSize * 0.96),
                    (int) (hexSize * 0.34),
                    12,
                    12
            );
            graphics.setColor(RESOURCE_TEXT);
            graphics.setFont(SMALL_BOLD_FONTS[zoomIndex]);
            drawCenteredString(
                    graphics,
                    resourcesShortName(resources),
                    center,
                    center + hexSize * 0.36
            );
        }

        if (key.buildingType() != null) {
            graphics.setColor(BUILDING_BACKGROUND);
            graphics.fillOval(
                    (int) (center - hexSize * 0.28),
                    (int) (center - hexSize * 0.92),
                    (int) (hexSize * 0.56),
                    (int) (hexSize * 0.36)
            );
            graphics.setColor(Color.WHITE);
            graphics.setFont(SMALL_BOLD_FONTS[zoomIndex]);
            drawCenteredString(
                    graphics,
                    buildingShortName(key.buildingType()),
                    center,
                    center - hexSize * 0.66
            );
        }
    }

    private void drawBorder(Graphics2D graphics, Polygon polygon, boolean discovered) {
        graphics.setColor(discovered ? HEX_BORDER : FOG_BORDER);
        graphics.setStroke(HEX_STROKE);
        graphics.drawPolygon(polygon);
    }

    private void drawFogMark(Graphics2D graphics, Polygon polygon) {
        Rectangle bounds = polygon.getBounds();
        graphics.setColor(FOG_OVERLAY);
        graphics.fillPolygon(polygon);
        graphics.setColor(FOG_TEXT);
        graphics.setFont(FOG_FONT);
        drawCenteredString(graphics, "?", bounds.getCenterX(), bounds.getCenterY() + 5);
    }

    private Polygon createHexPolygon(double centerX, double centerY, double hexSize) {
        int[] pointXs = new int[6];
        int[] pointYs = new int[6];
        for (int index = 0; index < 6; index++) {
            pointXs[index] = (int) Math.round(centerX + hexSize * HEX_COSINES[index]);
            pointYs[index] = (int) Math.round(centerY + hexSize * HEX_SINES[index]);
        }
        return new Polygon(pointXs, pointYs, 6);
    }

    private void configureRendering(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    private Color terrainColor(TerrainType terrain, Set<Resource> resources) {
        return resources.isEmpty()
                ? TERRAIN_COLORS.get(terrain)
                : RESOURCE_TERRAIN_COLORS.get(terrain);
    }

    private String resourcesShortName(Set<Resource> resources) {
        int mask = resourceMask(resources);
        String cachedLabel = resourceLabels.get(mask);
        if (cachedLabel != null) {
            return cachedLabel;
        }

        StringBuilder label = new StringBuilder();
        for (Resource resource : Resource.values()) {
            if ((mask & (1 << resource.ordinal())) != 0) {
                if (label.length() > 0) label.append('/');
                label.append(resourceName(resource));
            }
        }
        cachedLabel = label.toString();
        resourceLabels.put(mask, cachedLabel);
        return cachedLabel;
    }

    private String resourceName(Resource resource) {
        return switch (resource) {
            case WOOD -> "Wood";
            case STONE -> "Stone";
            case IRON -> "Iron";
            case FOOD -> "Food";
        };
    }

    private int resourceMask(Set<Resource> resources) {
        int mask = 0;
        for (Resource resource : resources) mask |= 1 << resource.ordinal();
        return mask;
    }

    private Set<Resource> resourcesFromMask(int mask) {
        EnumSet<Resource> resources = EnumSet.noneOf(Resource.class);
        for (Resource resource : Resource.values()) {
            if ((mask & (1 << resource.ordinal())) != 0) resources.add(resource);
        }
        return resources;
    }

    private String terrainShortName(TerrainType terrain) {
        return switch (terrain) {
            case TOWN_HALL -> "Capital";
            case PLAINS -> "Plains";
            case GRASSLAND -> "Grass";
            case FOREST -> "Forest";
            case MOUNTAIN -> "Mount";
        };
    }

    private String buildingShortName(BuildingType building) {
        return switch (building) {
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

    private void drawCenteredString(Graphics2D graphics, String text, double x, double y) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(text, (int) Math.round(x - metrics.stringWidth(text) / 2.0), (int) Math.round(y));
    }

    private static Font[] createFonts(int style, int normalSize, int minimumSize) {
        Font[] fonts = new Font[ZOOM_LEVELS.length];
        for (int index = 0; index < ZOOM_LEVELS.length; index++) {
            fonts[index] = new Font(
                    "SansSerif",
                    style,
                    Math.max(minimumSize, (int) (normalSize * ZOOM_LEVELS[index]))
            );
        }
        return fonts;
    }

    private static Map<TerrainType, Color> createTerrainColors() {
        Map<TerrainType, Color> colors = new EnumMap<>(TerrainType.class);
        colors.put(TerrainType.TOWN_HALL, new Color(132, 111, 82));
        colors.put(TerrainType.PLAINS, new Color(204, 179, 106));
        colors.put(TerrainType.GRASSLAND, new Color(99, 165, 94));
        colors.put(TerrainType.FOREST, new Color(59, 126, 76));
        colors.put(TerrainType.MOUNTAIN, new Color(124, 128, 132));
        return colors;
    }

    private static Map<TerrainType, Color> createResourceTerrainColors() {
        Map<TerrainType, Color> colors = new EnumMap<>(TerrainType.class);
        for (Map.Entry<TerrainType, Color> entry : TERRAIN_COLORS.entrySet()) {
            Color color = entry.getValue();
            colors.put(
                    entry.getKey(),
                    new Color(
                            Math.min(255, color.getRed() + 20),
                            Math.min(255, color.getGreen() + 20),
                            Math.min(255, color.getBlue() + 20)
                    )
            );
        }
        return colors;
    }

    private record TileKey(
            boolean discovered,
            TerrainType terrain,
            int resourceMask,
            BuildingType buildingType
    ) {
    }
}
