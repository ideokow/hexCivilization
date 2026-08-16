package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

public class PopulationBuilding extends Building {

    private PopulationType populationType;

    public PopulationBuilding(Player owner, BuildingType type, HexCoordinate position) {
        super(type, owner, position);

        if (type == BuildingType.VILLAGE) {
            populationType = PopulationType.VILLAGE;
        }
        else if (type == BuildingType.TOWN) {
            populationType = PopulationType.TOWN;
        }
        else {
            throw new IllegalArgumentException("Population building type must be VILLAGE or TOWN");
        }
    }

    public PopulationType getPopulationType() {
        return populationType;
    }
}
