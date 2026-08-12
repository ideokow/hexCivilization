package model.game.tribe.mission;

// TODO: add Trade   tribe reward
// TODO: add Coastal tribe reward

import model.game.townhall.TownHall;
import model.game.tribe.Tribe;

public abstract class Mission {

    private final Tribe tribe;
    private final MissionType missionType;
    private MissionState missionState;

    private final int deadLine;

    public Mission(Tribe tribe, MissionType missionType, int deadLine) {
        this.tribe = tribe;
        this.missionType = missionType;
        this.deadLine = deadLine;
        this.missionState = MissionState.AVAILABLE;
    }

    public abstract void payReward(TownHall townHall);

    public void acquireMission() {
        if (missionState.equals(MissionState.AVAILABLE)) {
            missionState = MissionState.ACTIVE;
        }
    }

    public abstract boolean checkRequirements();

    public void finishMission(TownHall townHall) {
        if (checkRequirements() && missionState.equals(MissionState.ACTIVE)) {
            missionState = MissionState.COMPLETED;
            payReward(townHall);
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