package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class Monument extends Building {
    public Monument(Player owner, HexCoordinate position) {
        super(BuildingType.MONUMENT, owner, position);
    }
}
