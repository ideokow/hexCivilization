package controller.game;

import model.game.hex.Resource;
import model.game.registry.UpKeepStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MessageFormat {

    public static String turnStatusAlert(int turnNumber, Map<Resource, Integer> resourceGenerated, UpKeepStatus upkeepStatus, boolean feedStatus) {
        String resourceAlert = formatResourceAlert(resourceGenerated);
        String upkeepAlert = formatUpkeepAlert(upkeepStatus);
        String feedAlert = formatFeedAlert(feedStatus);
        return "Turn " + turnNumber + " started | " + resourceAlert + " | " + upkeepAlert + " | " + feedAlert;
    }

    public static String formatResourceAlert(Map<Resource, Integer> generated) {
        if (generated == null || generated.isEmpty()) {
            return "No resources produced.";
        }

        List<String> parts = new ArrayList<>();
        for (Map.Entry<Resource, Integer> entry : generated.entrySet()) {
            int amount = entry.getValue();
            if (amount > 0) {
                parts.add("+" + amount + " " + entry.getKey().getDisplayName());
            }
        }

        return parts.isEmpty() ? "No resources produced." : "Produced: " + String.join(", ", parts);
    }

    public static String formatUpkeepAlert(UpKeepStatus upkeepStatus) {
        if (upkeepStatus == UpKeepStatus.SUCCESS) {
            return "upkeep paid successfully.";
        } else if (upkeepStatus == UpKeepStatus.SOME_BUILDINGS_RUINED) {
            return "some buildings ruined! no resources!!";
        } else if (upkeepStatus == UpKeepStatus.UP_KEEP_PAYMENT_FAILED) {
            return "upkeep payment failed for some buildings!";
        } else {
            return "";
        }
    }

    private static String formatFeedAlert(boolean feedStatus) {
        if (feedStatus) {
            return "The units were fed.";
        } else {
            return "Some units went hungry.";
        }
    }

    public static String formatStarvationAlert(int turnNumber) {
        return "Turn " + turnNumber + ": WARNING - starvation! Your civilization cannot feed its units. ";
    }

//    public static String formatUpgradeAlert(Upgrade upgrade) {
//        return "Upgrade complete: " + upgrade.getName() + "!";
//    }
//
//    public static String formatGeneratedAlert(UnitType unitType) {
//        return "New unit generated: " + unitType.getName() + "!";
//    }
}
