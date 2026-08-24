package model.game.townhall.opration;

import controller.game.UnitMap;
import model.game.building.MilitaryStable;
import model.game.townhall.TownHall;
import model.game.unit.UnitType;

public class GenerateCavalryOperation extends TownHallOperation {

    public static final int UNIT_GENERATION_TURN_COST = 3;

    private final TownHall townHall;
    private final UnitMap unitMap;
    private final MilitaryStable militaryStable;

    public GenerateCavalryOperation(TownHall townHall, UnitMap unitMap, OperationQueue queue, MilitaryStable militaryStable) {
        super(TownHallOperationType.GENERATE_UNIT, UNIT_GENERATION_TURN_COST, queue);
        this.townHall = townHall;
        this.unitMap = unitMap;
        this.militaryStable = militaryStable;
    }

    @Override
    protected void onReserve() {
        townHall.spendResources(UnitType.CAVALRY.getCost());
    }

    @Override
    protected void onComplete() {
        militaryStable.generateUnit(townHall, unitMap);
    }

    @Override
    protected void onCancel() {
        townHall.addResources(UnitType.CAVALRY.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        GenerationStatus status = militaryStable.canGenerateUnit(townHall, unitMap);
        if (status.equals(GenerationStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible(status.getMessage());
        }
    }
}
