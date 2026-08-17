package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;
import model.game.unit.UnitType;

public class Cavalry extends MilitaryUnit {
    public Cavalry(HexCoordinate position) {
        super(position, MilitaryType.CAVALRY);
    }

    public Cavalry(HexCoordinate position, Tribe tribe) {
        super(position, MilitaryType.CAVALRY, tribe);
    }

    @Override
    public UnitType getType() {
        return UnitType.CAVALRY;
    }
}
