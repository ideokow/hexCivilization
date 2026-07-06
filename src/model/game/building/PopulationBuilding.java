package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class PopulationBuilding extends Building {

    public PopulationBuilding(Player owner, BuildingType type, HexCoordinate position) {
        super(type, owner, position);
        if (type != BuildingType.VILLAGE && type != BuildingType.TOWN) {
            throw new IllegalArgumentException("Population building type must be VILLAGE or TOWN");
        }
    }
}
