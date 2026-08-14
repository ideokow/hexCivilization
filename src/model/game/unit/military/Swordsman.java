package model.game.unit.military;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.unit.UnitType;

public class Swordsman extends MilitaryUnit {
    public Swordsman(Player owner, HexCoordinate position) {
        super(owner, position, MilitaryType.SWORDSMAN);
    }

    @Override
    public UnitType getType() {
        return UnitType.SWORDSMAN;
    }
}
