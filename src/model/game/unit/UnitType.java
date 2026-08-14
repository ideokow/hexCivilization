package model.game.unit;

import model.game.hex.Resource;
import model.game.townhall.Level;

import java.util.HashMap;
import java.util.Map;

public enum UnitType {
    EXPLORER
            (8, 40, 3, "Explorer",
                    cost(0, 0, 0, 0), Level.LEVEL_1),
    WORKER
            (4, 40, 1, "Worker",
                    cost(0, 0, 0, 0), Level.LEVEL_1),
    BUILDER
            (5, 40, 1, "Builder",
                    cost(0, 0, 0, 0), Level.LEVEL_1),
    BORDER_EXPANDER
            ( 6, 50, 1, "Border-Expander",
                    cost(0, 0, 0, 0), Level.LEVEL_1),
    SWORDSMAN
            (10, 50, 1, "SwordsMan",
                    cost(5, 0, 0, 5), Level.LEVEL_1),
    ARCHER
            (10, 60, 2, "Archer",
                    cost(5, 0, 2, 5), Level.LEVEL_2),
    CAVALRY
            (16, 80, 2, "Cavalry",
                    cost(5, 0, 5, 5), Level.LEVEL_2);

    private final int eachTurnAP;
    private final int HP;
    private final int visibilityRadius;
    private final String name;
    private final Map<Resource, Integer> cost;
    private final Level minimumLevel;

    UnitType(
            int eachTurnAP,
            int HP,
            int visibilityRadius,
            String name,
            Map<Resource, Integer> cost,
            Level minimumLevel) {
        this.eachTurnAP = eachTurnAP;
        this.HP = HP;
        this.visibilityRadius = visibilityRadius;
        this.name = name;
        this.cost = cost;
        this.minimumLevel = minimumLevel;
    }

    public int getEachTurnAP() {
        return eachTurnAP;
    }

    public int getHP() {
        return HP;
    }

    public int getVisibilityRadius() {
        return visibilityRadius;
    }

    public String getName() {
        return name;
    }

    public Map<Resource, Integer> getCost() {
        return cost;
    }

    public Level getMinimumLevel() {
        return minimumLevel;
    }

    private static Map<Resource, Integer> cost(int food, int stone, int iron, int wood) {
        Map<Resource, Integer> resources = new HashMap<>();
        resources.put(Resource.FOOD , food );
        resources.put(Resource.STONE, stone);
        resources.put(Resource.IRON , iron );
        resources.put(Resource.WOOD , wood );
        return resources;
    }

    public static boolean isMilitary(UnitType unitType) {
        UnitType[] militaryTypes = new UnitType[]{
                UnitType.SWORDSMAN,
                UnitType.ARCHER,
                UnitType.CAVALRY
        };
        for (UnitType military : militaryTypes) {
            if (unitType.equals(military)) return true;
        }
        return false;
    }
}
