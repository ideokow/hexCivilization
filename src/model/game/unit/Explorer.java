package model.game.unit;

import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;

import java.util.List;

public class Explorer extends Unit {

    public Explorer(boolean ownedByPlayer, HexCoordinate position) {
        super(ownedByPlayer, position);
    }

    public void exploreMap(HexGrid hexGrid) {
        List<Hex> hexesInRange = hexGrid.hexesInRange(getPosition(), getType().getVisibilityRadius());
        for (Hex hex : hexesInRange) {
            if (!hexGrid.isDiscovered(hex.getCoordinate())) hexGrid.discover(hex.getCoordinate());
        }
    }

    @Override
    public UnitType getType() {
        return UnitType.EXPLORER;
    }
}
