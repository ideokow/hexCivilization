package model.game.trade;

public enum TradeStatus {
    SUCCESS
            ("Trade completed successfully."),
    NULL_ARGUMENT
            ("A required trade argument is missing."),
    INVALID_RESOURCE_PAIR
            ("The sold and received resources must be different."),
    INVALID_AMOUNT
            ("The sold amount must be positive."),
    INVALID_TURN
            ("The trade turn cannot be negative."),
    INSUFFICIENT_RESOURCES
            ("The Town Hall does not have enough of the sold resource."),
    INSUFFICIENT_STORAGE
            ("The Town Hall does not have enough storage space for the received resource."),
    BAZAAR_NOT_AVAILABLE
            ("The selected Bazaar is not available for trade."),
    BAZAAR_LEVEL_REQUIRED
            ("The Town Hall must be at least level 2 to use a Bazaar."),
    BAZAAR_OUTSIDE_TERRITORY
            ("The Bazaar must be inside the player's territory."),
    BAZAAR_ALREADY_USED_THIS_TURN
            ("A Bazaar trade has already been made this turn."),
    TRADING_POST_NOT_AVAILABLE
            ("The selected Trading Post is not available for trade."),
    TRADING_POST_OUTSIDE_TERRITORY
            ("The Trading Post must be inside the player's territory."),
    TRADING_POST_ALREADY_USED_THIS_TURN
            ("A Trading Post trade has already been made this turn."),
    TRIBE_NOT_AVAILABLE
            ("This tribe is not currently available for trade."),
    TRIBE_RESOURCE_NOT_SUPPORTED
            ("This tribe does not provide the selected resource."),
    TRIBE_ALREADY_USED_THIS_TURN
            ("A trade has already been made with this tribe this turn."),
    WRONG_TOWN_HALL
            ("The trade source belongs to another Town Hall.");

    private final String message;

    TradeStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
