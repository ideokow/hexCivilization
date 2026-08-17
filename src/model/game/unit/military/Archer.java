package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;
import model.game.unit.UnitType;

public class Archer extends MilitaryUnit {
    public Archer(HexCoordinate position) {
        super(position, MilitaryType.ARCHER);
    }

    public Archer(HexCoordinate position, Tribe tribe) {
        super(position, MilitaryType.ARCHER, tribe);
    }

    @Override
    public UnitType getType() {
        return UnitType.ARCHER;
    }
}
