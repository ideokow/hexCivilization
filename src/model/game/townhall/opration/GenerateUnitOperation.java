package model.game.townhall.opration;

import model.game.unit.UnitType;

public class GenerateUnitOperation extends TownHallOperation {

    public static final int UNIT_GENERATION_TURN_COST = 3;

    private final UnitType unitType;

    public GenerateUnitOperation(UnitType unitType, OperationQueue queue) {
        super(TownHallOperationType.GENERATE_UNIT, UNIT_GENERATION_TURN_COST, queue);
        this.unitType = unitType;
    }

    @Override
    protected void onReserve() {}

    @Override
    protected void onComplete() {
        getQueue().getTownHall().generateUnit(unitType);
    }

    @Override
    protected void onCancel() {}

    @Override
    protected OperationCheckResult canOperate() {
        if (getQueue().getTownHall().canGenerateUnit()) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible("Unit cap reached! can't generate new Unit.");
        }
    }
}
