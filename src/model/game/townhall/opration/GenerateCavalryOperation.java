package model.game.townhall.opration;

import model.game.building.MilitaryStable;
import model.game.unit.UnitType;

public class GenerateCavalryOperation extends TownHallOperation {

    public static final int UNIT_GENERATION_TURN_COST = 3;

    private final MilitaryStable militaryStable;

    public GenerateCavalryOperation(OperationQueue queue, MilitaryStable militaryStable) {
        super(TownHallOperationType.GENERATE_UNIT, UNIT_GENERATION_TURN_COST, queue);
        this.militaryStable = militaryStable;
    }

    @Override
    protected void onReserve() {
        getQueue().getTownHall().spendResources(UnitType.CAVALRY.getCost());
    }

    @Override
    protected void onComplete() {
        militaryStable.generateUnit(getQueue().getTownHall());
    }

    @Override
    protected void onCancel() {
        getQueue().getTownHall().addResources(UnitType.CAVALRY.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        GenerationStatus status = militaryStable.canGenerateUnit(getQueue().getTownHall());
        if (status.equals(GenerationStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible(status.getMessage());
        }
    }
}
