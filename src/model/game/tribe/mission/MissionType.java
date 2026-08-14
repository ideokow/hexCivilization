package model.game.tribe.mission;

import model.game.tribe.Tribe;

public enum MissionType {
    FOOD_STOREHOUSE(1),
    TRADE_ROUTE(1),
    MILITARY_ASSISTANCE(1),
    MINING_TOOLS(1),
    COASTAL_DEVELOPMENT(1);

    private final int deadLine;

    MissionType(int deadLine) {
        this.deadLine = deadLine;
    }

    public int getDeadLine() {
        return deadLine;
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