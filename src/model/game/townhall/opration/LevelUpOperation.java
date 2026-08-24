package model.game.townhall.opration;

import model.game.townhall.Level;
import model.game.townhall.TownHall;

public class LevelUpOperation extends TownHallOperation {

    private final TownHall townHall;
    private final Level level;

    public LevelUpOperation(TownHall townHall, Level level, OperationQueue queue) {
        super(TownHallOperationType.UPGRADE_TOWN_HALL_LEVEL, level.getTurnCost(), queue);
        this.townHall = townHall;
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    protected void onReserve() {
        if (!canOperate().isPossible()) return;
        townHall.spendResources(level.getCost());
    }

    @Override
    protected void onComplete() {
        Level currentLevel = townHall.getLevel();
        if (level.getLevelN() - currentLevel.getLevelN() != 1) return;
        townHall.setLevel(level);
    }

    @Override
    protected void onCancel() {
        if (!isCancelled()) return;
        townHall.addResources(level.getCost());
    }

    @Override
    protected OperationCheckResult canOperate() {
        Level currentLevel = townHall.getLevel();
        if (level.getLevelN() - currentLevel.getLevelN() == 1) {
            if (!townHall.canAfford(level.getCost())){
                cancel();
                return OperationCheckResult.impossible("You don't have enough resource to upgrade!");
            }
            return OperationCheckResult.possible();
        }
        else {
            return OperationCheckResult.impossible("You can't upgrade to this level!");
        }
    }
}
