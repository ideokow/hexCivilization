package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;

public class Worker extends Unit {

    private boolean isInBuilding = false;

    public Worker(HexCoordinate position) {
        super(position);
    }

    public Worker(HexCoordinate position, Tribe tribe) {
        super(position, tribe);
    }

    public boolean isInBuilding() {
        return isInBuilding;
    }

    public void setInBuilding(boolean inBuilding) {
        isInBuilding = inBuilding;
    }

    @Override
    public UnitType getType() {
        return UnitType.WORKER;
    }
}
