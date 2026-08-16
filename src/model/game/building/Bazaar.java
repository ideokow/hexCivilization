package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.townhall.TownHall;

public class Bazaar extends Building {

    public Bazaar(HexCoordinate position) {
        this(true, position);
    }

    public Bazaar(boolean ownedByPlayer, HexCoordinate position) {
        super(BuildingType.BAZAAR, ownedByPlayer, position);
    }

    public boolean isAvailableForTrade(TownHall townHall) {
        return townHall != null
                && isOwnedByPlayer()
                && !isRuined()
                && townHall.getLevel().getLevelN() >= 2;
    }
}
