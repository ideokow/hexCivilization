package model.game.townhall.opration;

import model.game.townhall.Level;

public class LevelUpOperation extends TownHallOperation {

    private final Level level;

    public LevelUpOperation(Level level, OperationQueue queue) {
        super(TownHallOperationType.UPGRADE_TOWN_HALL_LEVEL, level.getTurnCost(), queue);
        this.level = level;
    }

    @Override
    protected void onReserve() {
        if (!canOperate().isPossible()) return;
        getQueue().getTownHall().spendResources(level.getCost());
    }

    @Override
    protected void onComplete() {
        Level currentLevel = getQueue().getTownHall().getLevel();
        if (level.getLevelN() - currentLevel.getLevelN() != 1) return;
        getQueue().getTownHall().setLevel(level);
    }

    @Override
    protected void onCancel() {
        if (!isCancelled()) return;
        getQueue().getTownHall().addResources(level.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        Level currentLevel = getQueue().getTownHall().getLevel();
        if (level.getLevelN() - currentLevel.getLevelN() == 1) {
            if (!getQueue().getTownHall().canAfford(level.getCost())){
                cancel();
                return OperationCheckResult.impossible("You don't have enough resource to upgrade!");
            }
            return OperationCheckResult.possible();
        }
        else {
            cancel();
            return OperationCheckResult.impossible("You can't upgrade to this level!");
        }
    }
}
