package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;

public class Builder extends Unit {

    private int charges;

    public Builder(HexCoordinate position) {
        super(position);
        initialCharge();
    }

    public Builder(HexCoordinate position, Tribe tribe) {
        super(position, tribe);
        initialCharge();
    }

    private void initialCharge() {
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
