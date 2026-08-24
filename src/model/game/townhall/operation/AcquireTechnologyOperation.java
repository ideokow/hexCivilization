package model.game.townhall.operation;

import model.game.townhall.Technology;
import model.game.townhall.TechnologyAcquireStatus;
import model.game.townhall.TownHall;

public class AcquireTechnologyOperation extends TownHallOperation {

    private final TownHall townHall;
    private final Technology technology;

    public AcquireTechnologyOperation(TownHall townHall, Technology technology, OperationQueue queue) {
        super(TownHallOperationType.ACQUIRE_TECHNOLOGY, technology.getTurnsCost(), queue);
        this.townHall = townHall;
        this.technology = technology;
    }

    public Technology getTechnology() {
        return technology;
    }

    @Override
    protected void onReserve() {
        if (!canOperate().isPossible()) return;
        townHall.spendResources(technology.getAcquireCost());
    }

    @Override
    protected void onComplete() {
        TechnologyAcquireStatus canAcquire = townHall.getTechnologies().canAcquire(technology);
        if (
            canAcquire.equals(TechnologyAcquireStatus.SUCCESS) ||
            canAcquire.equals(TechnologyAcquireStatus.NOT_ENOUGH_RESOURCE)
        ) {
            townHall.getTechnologies().acquire(technology);
            return;
        }
        onCancel();
    }

    @Override
    protected void onCancel() {
        if (!isCancelled()) return;
        townHall.addResources(technology.getAcquireCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        TechnologyAcquireStatus canAcquire = townHall.getTechnologies().canAcquire(technology);
        if (canAcquire.equals(TechnologyAcquireStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible(canAcquire.getMessage());
        }
    }
}
