package model.game;

public class BorderExpander extends Unit {

    public BorderExpander(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return null;
    }
}
