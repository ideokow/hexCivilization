package model.game.unit;

import model.game.hex.Hex;
import model.game.player.Player;

public class Explorer extends Unit {

    public Explorer(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.EXPLORER;
    }
}
