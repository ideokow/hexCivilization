package model.game.unit;

import model.game.hex.Hex;
import model.game.player.Player;

public class BorderExpander extends Unit {

    public BorderExpander(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.BORDER_EXPANDER;
    }
}
