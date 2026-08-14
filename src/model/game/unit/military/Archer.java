package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.unit.UnitType;

public class Archer extends MilitaryUnit {
    public Archer(Player owner, HexCoordinate position) {
        super(owner, position, MilitaryType.ARCHER);
    }

    @Override
    public UnitType getType() {
        return UnitType.ARCHER;
    }
}
