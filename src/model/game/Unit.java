package model.game;

import java.util.Objects;

/*
Units main model
 */
public abstract class Unit {

    private final String unitID;

    public Unit(String unitID) {
        // check if unitID is not null
        this.unitID = Objects.requireNonNull(unitID, "unitID");
    }

    public String getUnitID() {
        return unitID;
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
