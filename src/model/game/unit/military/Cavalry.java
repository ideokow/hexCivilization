package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.unit.UnitType;

public class Cavalry extends MilitaryUnit {
    public Cavalry(boolean ownedByPlayer, HexCoordinate position) {
        super(ownedByPlayer, position, MilitaryType.CAVALRY);
    }

    @Override
    public UnitType getType() {
        return UnitType.CAVALRY;
    }
}
