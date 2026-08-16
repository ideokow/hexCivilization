package model.game.building;

import model.game.hex.HexCoordinate;

public class TradingPost extends Building {

    public TradingPost(HexCoordinate position) {
        super(BuildingType.TRADING_POST, false, position);
    }

    public boolean isAvailableForTrade() {
        return !isOwnedByPlayer() && !isRuined();
    }
}
