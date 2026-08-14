package model.game.townhall.opration;

import model.game.unit.UnitType;

public class GenerateUnitOperation extends TownHallOperation {

    public static final int UNIT_GENERATION_TURN_COST = 3;

    private final UnitType unitType;

    public GenerateUnitOperation(UnitType unitType, OperationQueue queue) {
        super(TownHallOperationType.GENERATE_UNIT, UNIT_GENERATION_TURN_COST, queue);
        this.unitType = unitType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    protected void onReserve() {
        getQueue().getTownHall().spendResources(unitType.getCost());
    }

    @Override
    protected void onComplete() {
        getQueue().getTownHall().generateUnit(unitType);
    }

    @Override
    protected void onCancel() {
        getQueue().getTownHall().addResources(unitType.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        GenerationStatus status = getQueue().getTownHall().canGenerateUnit(unitType);
        if (status.equals(GenerationStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible(status.getMessage());
        }
    }
}
