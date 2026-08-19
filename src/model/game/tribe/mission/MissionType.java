package model.game.tribe.mission;

import model.game.tribe.Tribe;

public enum MissionType {
    FOOD_STOREHOUSE(5,
            "Deliver 20 Wood and 10 Stone to the tribe within 5 turns of accepting the mission."),
    TRADE_ROUTE(10,
            "Build an unbroken road path from any of your buildings to a hex adjacent to the tribe’s camp within 10 turns of accepting the mission."),
    MILITARY_ASSISTANCE(8,
            "Defeat an enemy unit (another tribe's unit) within 5 hexes of the tribe’s camp within 8 turns of accepting the mission"),
    MINING_TOOLS(6,
            "Deliver 15 Wood and 10 Iron to the tribe within 6 turns of accepting the mission."),
    COASTAL_DEVELOPMENT(10,
            "Build a Dock within 4 hexes of the tribe’s camp within 10 turns of accepting the mission.");

    private final int deadLine;
    private final String description;

    MissionType(int deadLine, String description) {
        this.deadLine = deadLine;
        this.description = description;
    }

    public int getDeadLine() {
        return deadLine;
    }

    public String getDescription() {
        return description;
    }

    public static Mission getMissionFromTribeType(Tribe tribe) {
        switch (tribe.getTribeType()) {
            case FARMER -> {
                return new FarmerMission(tribe);
            }
            case TRADER -> {
                return new TraderMission(tribe);
            }
            case FIGHTER -> {
                return new FighterMission(tribe);
            }
            case MOUNTAINEER -> {
                return new MountaineerMission(tribe);
            }
            case COASTAL -> {
                return new CoastalMission(tribe);
            }
            default -> {
                return null;
            }
        }
    }
}