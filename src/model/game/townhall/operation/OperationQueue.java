package model.game.townhall.operation;

import java.io.Serializable;

public class OperationQueue implements Serializable {

    private TownHallOperation inQueueOperation;

    public OperationQueue() {}

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

    public TownHallOperation getInQueueOperation() {
        return inQueueOperation;
    }
}
