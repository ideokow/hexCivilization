package model.game.unit;

import model.game.hex.Hex;
import model.game.player.Player;

public class Builder extends Unit {

    private int charges;

    public Builder(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
        charges = 3;
    }

    @Override
    public UnitType getType() {
        return UnitType.BUILDER;
    }

    public int getCharges() {
        return charges;
    }

    public boolean hasCharges() {
        return charges > 0;
    }

    public void consumeCharge() {
        if (charges > 0) {
            charges--;
        }
    }
}
