package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;
import model.game.unit.UnitType;

public class Swordsman extends MilitaryUnit {
    public Swordsman(HexCoordinate position) {
        super(position, MilitaryType.SWORDSMAN);
    }

    public Swordsman(HexCoordinate position, Tribe tribe) {
        super(position, MilitaryType.SWORDSMAN, tribe);
    }

    @Override
    public UnitType getType() {
        return UnitType.SWORDSMAN;
    }
}
