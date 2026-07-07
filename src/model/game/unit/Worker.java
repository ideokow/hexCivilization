package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Worker extends Unit {

    private boolean isInBuilding = false;

    public Worker(Player owner, HexCoordinate position) {
        super(owner, position);
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
