package model.game.unit;

import model.game.hex.Hex;
import model.game.player.Player;

public class Builder extends Unit {

    public Builder(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return null;
    }
}
