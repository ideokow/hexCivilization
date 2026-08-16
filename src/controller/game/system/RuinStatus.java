package controller.game.system;

public enum RuinStatus {
    SUCCESS("Building ruined successfully!"),
    BUILDING_RUINED_ALREADY("Building is ruined already!"),
    NOT_A_BUILDER("Selected unit is not a builder!"),
    LOW_AP("Builder does not have enough AP!"),
    NULL_ERR("Error: Building is not in the map."),
    CANT_RUIN_TOWN_HALL("You cant ruin town hall."),
    CANT_RUIN_TRADING_POST("You cannot ruin the neutral Trading Post."),
    CANT_RUIN_TRIBE_CAMP("You cannot ruin an active tribal camp."),
    NOT_ENOUGH_CHARGE("Builder does not have enough charge!"),
    BUILDER_IS_NOT_HERE("Builder is not in building hex!");

    private final String message;

    RuinStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
