package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class BorderExpander extends Unit {

    public BorderExpander(Player owner, HexCoordinate position) {
        super(owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.BORDER_EXPANDER;
    }
}
