package controller.game;

import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.hex.TerrainType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapLoader {

    private static final Pattern HEXES_SECTION_PATTERN = Pattern.compile("\"hexes\"\\s*:\\s*\\[(.*?)]\\s*,\\s*\"discovered\"", Pattern.DOTALL);
    private static final Pattern DISCOVERED_SECTION_PATTERN = Pattern.compile("\"discovered\"\\s*:\\s*\\[(.*?)]\\s*}", Pattern.DOTALL);
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*(-?\\d+)");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"([A-Z_]+)\"");
    private static final Pattern RESOURCES_PATTERN = Pattern.compile("\"resources\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern RESOURCE_VALUE_PATTERN = Pattern.compile("\"([A-Z_]+)\"");

    public HexGrid loadMap01() throws IOException {
        return load(Path.of("resources", "map", "map01.json"));
    }

    public HexGrid load(String filePath) throws IOException {
        return load(Path.of(filePath));
    }

    public HexGrid load(Path path) throws IOException {
        String json = Files.readString(path);
        List<Hex> hexes = parseHexes(json);
        HexGrid grid = new HexGrid(hexes);
        parseDiscovered(json).forEach(grid::discover);
        return grid;
    }

    private List<Hex> parseHexes(String json) {
        String section = findSection(HEXES_SECTION_PATTERN, json, "hexes");
        List<Hex> hexes = new ArrayList<>();
        Matcher matcher = OBJECT_PATTERN.matcher(section);

        while (matcher.find()) {
            String object = matcher.group(1);
            HexCoordinate coordinate = new HexCoordinate(readInt(object, "q"), readInt(object, "r"));
            TerrainType terrain = TerrainType.valueOf(readString(object, "terrain"));
            Set<Resource> resources = readResources(object);
            hexes.add(new Hex(coordinate, terrain, resources));
        }

        return hexes;
    }

    private List<HexCoordinate> parseDiscovered(String json) {
        String section = findSection(DISCOVERED_SECTION_PATTERN, json, "discovered");
        List<HexCoordinate> discovered = new ArrayList<>();
        Matcher matcher = OBJECT_PATTERN.matcher(section);

        while (matcher.find()) {
            String object = matcher.group(1);
            discovered.add(new HexCoordinate(readInt(object, "q"), readInt(object, "r")));
        }

        return discovered;
    }

    private String findSection(Pattern pattern, String json, String sectionName) {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing JSON section: " + sectionName);
        }
        return matcher.group(1);
    }

    private int readInt(String object, String fieldName) {
        Matcher matcher = Pattern.compile(String.format(NUMBER_PATTERN.pattern(), fieldName)).matcher(object);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing integer field: " + fieldName);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String readString(String object, String fieldName) {
        Matcher matcher = Pattern.compile(String.format(STRING_PATTERN.pattern(), fieldName)).matcher(object);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing string field: " + fieldName);
        }
        return matcher.group(1);
    }

    private Set<Resource> readResources(String object) {
        Set<Resource> resources = EnumSet.noneOf(Resource.class);
        Matcher resourcesMatcher = RESOURCES_PATTERN.matcher(object);
        if (!resourcesMatcher.find()) {
            return resources;
        }

        Matcher resourceMatcher = RESOURCE_VALUE_PATTERN.matcher(resourcesMatcher.group(1));
        while (resourceMatcher.find()) {
            resources.add(Resource.valueOf(resourceMatcher.group(1)));
        }
        return resources;
    }
}
