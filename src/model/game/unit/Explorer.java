package model.game.unit;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Explorer extends Unit {

    public Explorer(Player owner, HexCoordinate position) {
        super(owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.EXPLORER;
    }
}
