package model.game.building;

import model.game.tribe.Tribe;

public class TribeCamp extends Building {

    private final Tribe tribe;

    public TribeCamp(Tribe tribe) {
        super(BuildingType.TRIBE_CAMP, false, tribe.getLocation());
        this.tribe = tribe;
    }

    public Tribe getTribe() {
        return tribe;
    }
}
