package model.game.building;

import model.game.player.Player;

public class PopulationBuilding extends Building {

    public PopulationBuilding(BuildingType type) {
        this(null, type);
    }

    public PopulationBuilding(Player owner, BuildingType type) {
        super(type, owner);
        if (type != BuildingType.VILLAGE && type != BuildingType.TOWN) {
            throw new IllegalArgumentException("Population building type must be VILLAGE or TOWN");
        }
    }
}
