package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Road extends Building {

    public Road(boolean ownedByPlayer, HexCoordinate position) {
        super(BuildingType.ROAD, ownedByPlayer, position);
    }
}
