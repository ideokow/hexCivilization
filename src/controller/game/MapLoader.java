package controller.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

public class MapLoader {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
    map X : normal map
     */
    public HexGrid loadMapX(int X) throws IOException {
        String tag = String.valueOf(X);
        if (tag.length() == 1) tag = "0" + tag;

        return load(Path.of("resources", "map", "map" + tag + ".json"));
    }

    public HexGrid load(Path path) throws IOException {
        JsonNode root = objectMapper.readTree(path.toFile());

        HexGrid grid = new HexGrid(parseHexes(root));
        parseDiscovered(root).forEach(grid::discover);
        return grid;
    }

    private List<Hex> parseHexes(JsonNode root) {
        JsonNode hexesNode = requireSection(root, "hexes");
        List<Hex> hexes = new ArrayList<>();

        for (JsonNode hexNode : hexesNode) {
            HexCoordinate coordinate = parseCoordinate(hexNode);
            TerrainType terrain = TerrainType.valueOf(hexNode.get("terrain").asText());
            Set<Resource> resources = parseResources(hexNode);
            hexes.add(new Hex(coordinate, terrain, resources));
        }

        return hexes;
    }

    private List<HexCoordinate> parseDiscovered(JsonNode root) {
        JsonNode discoveredNode = requireSection(root, "discovered");
        List<HexCoordinate> discovered = new ArrayList<>();

        for (JsonNode coordinateNode : discoveredNode) {
            discovered.add(parseCoordinate(coordinateNode));
        }

        return discovered;
    }

    private Map<HexCoordinate, TribeType> parseTribes(JsonNode root) {
        JsonNode tribesNode = requireSection(root, "tribes");
        Map<HexCoordinate, TribeType> tribes = new HashMap<>();

        for (JsonNode tribeNode : tribesNode) {
            JsonNode qNode = tribeNode.get("q");
            JsonNode rNode = tribeNode.get("r");
            JsonNode typeNode = tribeNode.get("type");

            int q = qNode.asInt();
            int r = rNode.asInt();

            HexCoordinate coordinate = new HexCoordinate(q, r);
            TribeType type = toTribeType(typeNode.asText());

            tribes.put(coordinate, type);
        }

        return tribes;
    }

    private TribeType toTribeType(String text) {
        return switch (text) {
            case "FARMER"      -> TribeType.FARMER;
            case "FIGHTER"     -> TribeType.FIGHTER;
            case "TRADER"      -> TribeType.TRADER;
            case "MOUNTAINEER" -> TribeType.MOUNTAINEER;
            case "COASTAL"     -> TribeType.COASTAL;
            default -> null;
        };
    }

    public HexGrid loadTribes(int X, HexGrid grid, TownHall townHall) throws IOException {
        String tag = String.valueOf(X);
        if (tag.length() == 1) tag = "0" + tag;

        Path path = Path.of("resources", "map", "map" + tag + ".json");
        Map<HexCoordinate, TribeType> tribes = parseTribes(objectMapper.readTree(path.toFile()));

        for (HexCoordinate position : tribes.keySet()) {
            grid.get(position).setBuilding(new TribeCamp(new Tribe(tribes.get(position), position, townHall)));
        }

        return grid;
    }

    private HexCoordinate parseCoordinate(JsonNode node) {
        return new HexCoordinate(node.get("q").asInt(), node.get("r").asInt());
    }

    private Set<Resource> parseResources(JsonNode hexNode) {
        Set<Resource> resources = EnumSet.noneOf(Resource.class);

        // "resources" field is optional; return empty set if missing
        JsonNode resourcesNode = hexNode.get("resources");
        if (resourcesNode == null || !resourcesNode.isArray()) {
            return resources;
        }

        for (JsonNode resourceNode : resourcesNode) {
            resources.add(Resource.valueOf(resourceNode.asText()));
        }

        return resources;
    }

    private JsonNode requireSection(JsonNode root, String sectionName) {
        JsonNode section = root.get(sectionName);
        if (section == null || !section.isArray()) {
            throw new IllegalArgumentException("Missing JSON section: " + sectionName);
        }
        return section;
    }
}
