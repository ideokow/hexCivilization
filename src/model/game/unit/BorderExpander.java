package model.game.unit;

import controller.game.GameEngine;
import controller.game.UnitMap;
import model.game.townhall.TownHall;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.player.Player;
import model.game.tribe.Tribe;

import java.util.List;

public class BorderExpander extends Unit {
    public BorderExpander(HexCoordinate position) {
        super(position);
    }

    public BorderExpander(HexCoordinate position, Tribe tribe) {
        super(position, tribe);
    }

    /*
    this function adds in range hexes to territory
     */
    public void expand(HexGrid hexGrid, Player player, UnitMap unitMap) {
        List<Hex> hexesInRange = hexGrid.hexesInRange(getPosition(), getType().getVisibilityRadius());
        for (Hex hex : hexesInRange) {
            if (!player.ownsTerritory(hex.getCoordinate()) && hexGrid.isDiscovered(hex.getCoordinate())) {
                player.addTerritory(hex.getCoordinate());
            }
        }
        consume(unitMap);
    }

    private void consume(UnitMap unitMap) {
        unitMap.killUnit(this);
    }

    @Override
    public UnitType getType() {
        return UnitType.BORDER_EXPANDER;
    }
}
