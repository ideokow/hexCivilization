package model.game.townhall.opration;

public abstract class TownHallOperation {

    private final TownHallOperationType type;

    private int turnsRemaining;
    private boolean cancelled;

    private OperationQueue queue;

    public TownHallOperation(TownHallOperationType type, int turnsRemaining, OperationQueue queue) {
        this.type = type;
        this.turnsRemaining = turnsRemaining;
        this.queue = queue;
        cancelled = false;
    }

    public void tick() {
        turnsRemaining -= 1;
        if (turnsRemaining == 0) {
            onComplete();
            leaveQueue();
        }
    }

    public void cancel() {
        cancelled = true;
        onCancel();
        leaveQueue();
    }

    protected void leaveQueue() {
        queue.cleanQueue();
        queue = null;
    }

    protected abstract void onComplete();

    protected abstract void onCancel();

    protected abstract void onReserve();

    protected abstract OperationCheckResult canOperate();

    public TownHallOperationType getType() {
        return type;
    }

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public OperationQueue getQueue() {
        return queue;
    }
}
