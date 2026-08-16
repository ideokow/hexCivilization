package model.game.building;

import model.game.hex.HexCoordinate;

public class Monument extends Building {
    public Monument(boolean ownedByPlayer, HexCoordinate position) {
        super(BuildingType.MONUMENT, ownedByPlayer, position);
    }
}
