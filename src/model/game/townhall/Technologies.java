package model.game.townhall;

import java.util.ArrayList;
import java.util.List;

public class Technologies {

    private final TownHall townHall;
    private final List<Technology> acquiredTechnologies;

    public Technologies(TownHall townHall) {
        this.townHall = townHall;
        acquiredTechnologies = new ArrayList<>();
    }

    public TechnologyAcquireStatus canAcquire(Technology toAcquire) {
        if (acquiredTechnologies.contains(toAcquire)) {
            return TechnologyAcquireStatus.TECHNOLOGY_ACQUIRED;
        }
        if (toAcquire.getTechnologyDependency() != null
                && !acquiredTechnologies.contains(toAcquire.getTechnologyDependency())) {
            return TechnologyAcquireStatus.BAD_HIERARCHY;
        }
        if (toAcquire.getLevelDependency() != null
                && townHall.getLevel().getLevelN()
                < toAcquire.getLevelDependency().getLevelN()) {
            return TechnologyAcquireStatus.TOWN_HALL_LEVEL_TOO_LOW;
        }
        if (!townHall.canAfford(toAcquire.getAcquireCost())) {
            return TechnologyAcquireStatus.NOT_ENOUGH_RESOURCE;
        }
        return TechnologyAcquireStatus.SUCCESS;
    }

    public void acquire(Technology toAcquire) {
        acquiredTechnologies.add(toAcquire);
    }

    public boolean isAcquired(Technology technology) {
        return acquiredTechnologies.contains(technology);
    }

    public List<Technology> getAcquiredTechnologies() {
        return new ArrayList<>(acquiredTechnologies);
    }
}
