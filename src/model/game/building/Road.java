package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Road extends Building {

    public Road(Player owner, HexCoordinate position) {
        super(BuildingType.ROAD, owner, position);
    }
}
