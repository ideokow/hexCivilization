package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.unit.UnitType;

public class Cavalry extends MilitaryUnit {
    public Cavalry(Player owner, HexCoordinate position) {
        super(owner, position, MilitaryType.CAVALRY);
    }

    @Override
    public UnitType getType() {
        return UnitType.CAVALRY;
    }
}
