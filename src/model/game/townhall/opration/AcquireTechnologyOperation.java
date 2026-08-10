package model.game.townhall.opration;

import model.game.townhall.Technology;
import model.game.townhall.TechnologyAcquireStatus;

public class AcquireTechnologyOperation extends TownHallOperation {

    private final Technology technology;

    public AcquireTechnologyOperation(Technology technology, OperationQueue queue) {
        super(TownHallOperationType.ACQUIRE_TECHNOLOGY, technology.getTurnsCost(), queue);
        this.technology = technology;
    }

    @Override
    protected void onReserve() {
        if (!canOperate().isPossible()) return;
        getQueue().getTownHall().spendResources(technology.getAcquireCost());
    }

    @Override
    protected void onComplete() {
        TechnologyAcquireStatus canAcquire = getQueue().getTownHall().getTechnologies().canAcquire(technology);
        if (
            canAcquire.equals(TechnologyAcquireStatus.TECHNOLOGY_ACQUIRED) ||
            canAcquire.equals(TechnologyAcquireStatus.BAD_HIERARCHY)
        ) {
            onCancel();
            return;
        }
        getQueue().getTownHall().getTechnologies().acquire(technology);
    }

    @Override
    protected void onCancel() {
        if (!isCancelled()) return;
        getQueue().getTownHall().addResources(technology.getAcquireCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        TechnologyAcquireStatus canAcquire = getQueue().getTownHall().getTechnologies().canAcquire(technology);
        if (canAcquire.equals(TechnologyAcquireStatus.SUCCESS)) {
            return OperationCheckResult.possible();
        }
        else {
            cancel();
            return OperationCheckResult.impossible(canAcquire.getMessage());
        }
    }
}
