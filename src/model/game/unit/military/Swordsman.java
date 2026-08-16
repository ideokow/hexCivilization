package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.unit.UnitType;

public class Swordsman extends MilitaryUnit {
    public Swordsman(boolean ownedByPlayer, HexCoordinate position) {
        super(ownedByPlayer, position, MilitaryType.SWORDSMAN);
    }

    @Override
    public UnitType getType() {
        return UnitType.SWORDSMAN;
    }
}
