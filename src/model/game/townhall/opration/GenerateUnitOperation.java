package model.game.townhall.opration;

import controller.game.map.UnitMap;
import model.game.townhall.TownHall;
import model.game.unit.UnitType;

public class GenerateUnitOperation extends TownHallOperation {

    public static final int UNIT_GENERATION_TURN_COST = 3;

    private final TownHall townHall;
    private final UnitMap unitMap;
    private final UnitType unitType;

    public GenerateUnitOperation(TownHall townHall, UnitMap unitMap, UnitType unitType, OperationQueue queue) {
        super(TownHallOperationType.GENERATE_UNIT, UNIT_GENERATION_TURN_COST, queue);
        this.townHall = townHall;
        this.unitMap = unitMap;
        this.unitType = unitType;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    protected void onReserve() {
        townHall.spendResources(unitType.getCost());
    }

    @Override
    protected void onComplete() {
        townHall.generateUnit(unitMap, unitType);
    }

    @Override
    protected void onCancel() {
        townHall.addResources(unitType.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        GenerationStatus status = townHall.canGenerateUnit(unitType, unitMap);
        if (status.equals(GenerationStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible(status.getMessage());
        }
    }
}
