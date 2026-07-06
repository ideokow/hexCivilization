package model.game;

import java.util.Objects;

/*
Units main model
 */
public abstract class Unit {

    private final String unitID;
    private final Player owner;
    private int currentAP;
    private Hex position;

    public Unit(String unitID, Player owner, Hex position) {
        // check if unitID is not null
        this.unitID = Objects.requireNonNull(unitID, "unitID");
        this.owner = owner;
        this.position = position;
    }

    public String getUnitID() {
        return unitID;
    }

    public abstract UnitType getType();

    public void resetAP() {
        currentAP = getType().getEachTurnAP();
    }

    public int getCurrentAP() {
        return currentAP;
    }

    public Hex getPosition() {
        return position;
    }

    public void setPosition(Hex position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        return unitID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Unit)) return false;
        return unitID.equals(((Unit) obj).unitID);
    }
}
