package view.game;

import model.game.building.Building;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;

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