package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Worker extends Unit {

    public Worker(Player owner, HexCoordinate position) {
        super(owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.WORKER;
    }
}
