package model.game;

public class Builder extends Unit {

    public Builder(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return null;
    }
}
