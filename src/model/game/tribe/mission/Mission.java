package model.game.tribe.mission;

import model.game.hex.Resource;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

import java.util.HashMap;
import java.util.Map;

public abstract class Mission {

    private final Tribe tribe;
    private final MissionType missionType;
    private MissionState missionState;

    private final int deadLine;

    private final Map<Resource, Integer> missionWarehouse;

    public Mission(Tribe tribe, MissionType missionType) {
        this.tribe = tribe;
        this.missionType = missionType;
        this.deadLine = missionType.getDeadLine();
        this.missionState = MissionState.AVAILABLE;
        missionWarehouse = new HashMap<>();
    }

    public abstract void payReward();

    public void acquireMission() {
        if (missionState.equals(MissionState.AVAILABLE)) {
            missionState = MissionState.ACTIVE;
        }
    }

    public abstract boolean checkRequirements();

    public void finishMission(TownHall townHall) {
        if (checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.COMPLETED;
            payReward();
        }
    }

    public void failMission() {
        if (!checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.FAILED;
        }
    }

    public void cancelMission() {
        if (!checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.FAILED;
        }
    }

    public void addResource(Resource resource, int amount) {
        if (amount <= 0) return;
        if (!missionWarehouse.containsKey(resource)) {
            missionWarehouse.put(resource, 0);
        }
        missionWarehouse.put(resource, missionWarehouse.get(resource) + amount);
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
}