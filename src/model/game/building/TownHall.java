package model.game.building;

import model.game.player.Player;

public class TownHall extends Building {

    public TownHall(Player owner) {
        super(BuildingType.TOWN_HALL, owner);
    }
}
