package model.game;

public class Worker extends Unit {

    public Worker(String unitID, Player owner, Hex position) {
        super(unitID, owner, position);
    }

    @Override
    public UnitType getType() {
        return UnitType.WORKER;
    }
}
