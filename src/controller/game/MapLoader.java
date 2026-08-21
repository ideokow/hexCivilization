package controller.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.game.building.TradingPost;
import model.game.building.TribeCamp;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.hex.TerrainType;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;
import model.game.tribe.TribeType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String HEXES = "hexes";
    private static final String DISCOVERED = "discovered";
    private static final String TRIBES = "tribes";
    private static final String TRADING_POSTS = "trading-posts";

    private final Map<Path, JsonNode> rootCache = new HashMap<>();

    // --- paths ---

    public static Path mapPath(int index) {
        return Path.of("resources", "map", String.format("map%02d.json", index));
    }

    // --- hex grid ---

    public HexGrid loadMap(int index) throws IOException {
        return load(mapPath(index));
    }

    public HexGrid load(Path path) throws IOException {
        JsonNode root = readRoot(path);

        HexGrid grid = new HexGrid(parseArray(root, HEXES, this::parseHex, true));
        parseArray(root, DISCOVERED, this::parseCoordinate, true).forEach(grid::discover);
        return grid;
    }

    // --- tribes ---

    public List<Tribe> loadTribes(int index, TownHall townHall) throws IOException {
        return loadTribes(mapPath(index), townHall);
    }

    public List<Tribe> loadTribes(Path path, TownHall townHall) throws IOException {
        List<Tribe> tribes = new ArrayList<>();

        for (JsonNode tribeNode : requireSection(readRoot(path), TRIBES)) {
            TribeType type = parseTribeType(tribeNode.get("type"));
            if (type != null) {
                tribes.add(new Tribe(type, parseCoordinate(tribeNode), townHall));
            }
        }
        return tribes;
    }

    public void loadTribesInGrid(HexGrid grid, List<Tribe> tribes) {
        for (Tribe tribe : tribes) {
            Hex hex = grid.get(tribe.getLocation());
            if (hex != null) {
                hex.setBuilding(new TribeCamp(tribe));
            }
        }
    }

    // --- trading posts ---

    public List<HexCoordinate> loadTradingPosts(int index) throws IOException {
        return loadTradingPosts(mapPath(index));
    }

    public List<HexCoordinate> loadTradingPosts(Path path) throws IOException {
        return parseArray(readRoot(path), TRADING_POSTS, this::parseCoordinate, false);
    }

    public void loadTradingPostsInGrid(HexGrid grid, List<HexCoordinate> locations) {
        for (HexCoordinate location : locations) {
            Hex hex = grid.get(location);
            if (hex != null) {
                hex.setBuilding(new TradingPost(location));
            }
        }
    }

    // --- parsing ---

    private Hex parseHex(JsonNode hexNode) {
        return new Hex(
                parseCoordinate(hexNode),
                TerrainType.valueOf(hexNode.get("terrain").asText()),
                parseResources(hexNode));
    }

    private HexCoordinate parseCoordinate(JsonNode node) {
        return new HexCoordinate(node.get("q").asInt(), node.get("r").asInt());
    }

    private Set<Resource> parseResources(JsonNode hexNode) {
        Set<Resource> resources = EnumSet.noneOf(Resource.class);

        JsonNode resourcesNode = hexNode.get("resources");
        if (resourcesNode == null || !resourcesNode.isArray()) {
            return resources;
        }

        for (JsonNode resourceNode : resourcesNode) {
            resources.add(Resource.valueOf(resourceNode.asText()));
        }
        return resources;
    }

    private TribeType parseTribeType(JsonNode typeNode) {
        if (typeNode == null || typeNode.isNull()) {
            return null;
        }
        try {
            return TribeType.valueOf(typeNode.asText());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // --- utilities ---

    private JsonNode readRoot(Path path) throws IOException {
        JsonNode cached = rootCache.get(path);
        if (cached != null) {
            return cached;
        }
        JsonNode root = OBJECT_MAPPER.readTree(path.toFile());
        rootCache.put(path, root);
        return root;
    }

    private <T> List<T> parseArray(JsonNode root, String section,
                                   Function<JsonNode, T> parser, boolean required) {
        JsonNode sectionNode = root.get(section);
        if (sectionNode == null || !sectionNode.isArray()) {
            if (required) {
                throw new IllegalArgumentException("Missing JSON section: " + section);
            }
            return List.of();
        }

        List<T> values = new ArrayList<>(sectionNode.size());
        for (JsonNode element : sectionNode) {
            values.add(parser.apply(element));
        }
        return values;
    }

    private JsonNode requireSection(JsonNode root, String section) {
        JsonNode sectionNode = root.get(section);
        if (sectionNode == null || !sectionNode.isArray()) {
            throw new IllegalArgumentException("Missing JSON section: " + section);
        }
        return sectionNode;
    }
}
