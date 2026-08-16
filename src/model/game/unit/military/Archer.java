package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.unit.UnitType;

public class Archer extends MilitaryUnit {
    public Archer(boolean ownedByPlayer, HexCoordinate position) {
        super(ownedByPlayer, position, MilitaryType.ARCHER);
    }

    @Override
    public UnitType getType() {
        return UnitType.ARCHER;
    }
}
