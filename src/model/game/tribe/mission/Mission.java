package model.game.tribe.mission;

import model.game.hex.Resource;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Mission implements Serializable {

    private final Tribe tribe;
    private final MissionType missionType;
    private MissionState missionState;

    private final int deadLine;
    private int acquireTurn;

    private final Map<Resource, Integer> missionWarehouse;

    public Mission(Tribe tribe, MissionType missionType) {
        this.tribe = tribe;
        this.missionType = missionType;
        this.deadLine = missionType.getDeadLine();
        this.missionState = MissionState.AVAILABLE;
        missionWarehouse = new HashMap<>();
    }

    public abstract void payReward();

    public boolean acquireMission(int acquireTurn) {
        if (tribe.canAcquireMission() && missionState.equals(MissionState.AVAILABLE)) {
            missionState = MissionState.ACTIVE;
            this.acquireTurn = acquireTurn;
            return true;
        }
        else {
            return false;
        }
    }

    public abstract boolean checkRequirements();

    public void finishMission(TownHall townHall) {
        if (checkRequirements() && missionState.equals(MissionState.READY_TO_DELIVER)) {
            missionState = MissionState.COMPLETED;
            payReward();
        }
    }

    public void failMission() {
        if (!checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.FAILED;
        }
        tribe.failMission();
    }

    public void cancelMission() {
        if (!checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.FAILED;
        }
    }

    public void setIsReady() {
        if (missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.READY_TO_DELIVER;
        }
    }

    public void addResource(Resource resource, int amount) {
        if (amount <= 0) return;
        if (!missionWarehouse.containsKey(resource)) {
            missionWarehouse.put(resource, 0);
        }
        missionWarehouse.put(resource, missionWarehouse.get(resource) + amount);
    }

    public void addResource(Map<Resource, Integer> resources) {
        for (Resource resource : resources.keySet()) {
            addResource(resource, resources.get(resource));
        }
    }

    public Map<Resource, Integer> getMissionWarehouse() {
        return new HashMap<>(missionWarehouse);
    }

    public Tribe getTribe() {
        return tribe;
    }

    public MissionType getMissionType() {
        return missionType;
    }

    public MissionState getMissionState() {
        return missionState;
    }

    public int getDeadLine() {
        return deadLine;
    }

    public int getAcquireTurn() {
        return acquireTurn;
    }
}
