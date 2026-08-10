package model.game.townhall.opration;

import model.game.townhall.TownHall;

public class OperationQueue {

    private final TownHall townHall;
    private TownHallOperation inQueueOperation;

    public OperationQueue(TownHall townHall) {
        this.townHall = townHall;
    }

    public OperationCheckResult reserveOperation(TownHallOperation townHallOperation) {
        if (inQueueOperation != null) return OperationCheckResult.impossible("Theres another operation in queue!");
        OperationCheckResult canOperate = townHallOperation.canOperate();
        if (canOperate.isPossible()) {
            inQueueOperation = townHallOperation;
            inQueueOperation.onReserve();
        }
        return canOperate;
    }

    public void handleTurn() {
        if (inQueueOperation != null) {
            if (inQueueOperation.isCancelled()) inQueueOperation = null;
            else inQueueOperation.tick();
        }
    }

    public void cleanQueue() {
        if (inQueueOperation != null) {
            if (!inQueueOperation.isCancelled() && inQueueOperation.getTurnsRemaining() > 0) {
                inQueueOperation.cancel();
            }
            inQueueOperation = null;
        }
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public TownHallOperation getInQueueOperation() {
        return inQueueOperation;
    }
}
