package model.game.unit;

import model.game.hex.HexCoordinate;

public class Worker extends Unit {

    private boolean isInBuilding = false;

    public Worker(boolean ownedByPlayer, HexCoordinate position) {
        super(true, position);
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
