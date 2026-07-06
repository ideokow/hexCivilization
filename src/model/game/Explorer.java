package model.game;

public class Explorer extends Unit {

    public Explorer(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return null;
    }
}
