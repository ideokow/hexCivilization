package view.game;

import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.hex.Wall;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class ViewTextFormatter {

    private ViewTextFormatter() {
    }

    static String formatCoordinate(
            HexCoordinate coordinate
    ) {
        return "("
                + coordinate.getQ()
                + ", "
                + coordinate.getR()
                + ")";
    }

    static String formatResources(
            Set<Resource> resources
    ) {
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

    static String formatBuilding(Building building) {
        if (building == null) {
            return "None";
        }

        String ruinedSuffix =
                building.isRuined() ? " (Ruined)" : "";

        return pretty(building.getType()) + ruinedSuffix;
    }

    static String formatWall(Wall wall) {
        if (wall == null) {
            return "None";
        }

        return formatCoordinate(wall.getCoordinate1())
                + " - "
                + formatCoordinate(wall.getCoordinate2())
                + " ("
                + wall.getHp()
                + " HP)";
    }

    static String formatWalls(List<Wall> walls) {
        if (walls == null || walls.isEmpty()) {
            return "None";
        }

        List<String> wallDescriptions = new ArrayList<>();
        for (Wall wall : walls) {
            wallDescriptions.add(formatWall(wall));
        }
        wallDescriptions.sort(String::compareTo);
        return String.join(", ", wallDescriptions);
    }

    static String pretty(Enum<?> value) {
        String text = value
                .name()
                .toLowerCase()
                .replace('_', ' ');

        StringBuilder result =
                new StringBuilder(text.length());

        boolean capitalize = true;

        for (char character : text.toCharArray()) {
            if (capitalize && Character.isLetter(character)) {
                result.append(
                        Character.toUpperCase(character)
                );
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
}
