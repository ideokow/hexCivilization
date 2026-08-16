package model.game.tribe;

import model.game.building.TribeCamp;
import model.game.happiness.Happiness;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.townhall.TownHall;
import model.game.tribe.mission.Mission;
import model.game.tribe.mission.MissionState;
import model.game.tribe.mission.MissionType;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tribe {

    private static final int MIN_RELATION = -100;
    private static final int MAX_RELATION =  100;

    private static final int PEACE_RELATION = -10;
    private static final int PEACE_WAIT_TURNS = 3;

    private final TribeType tribeType;
    private final TribeCamp tribeCamp;
    private final HexCoordinate location;

    private final List<Unit> tribeUnits;

    private final TownHall relatedTownHall;
    private int relation;

    private Mission currentMission;

    // --- state parameters

    private boolean atWar;
    private boolean allianceActive;

    private boolean peaceRequestActive;
    private int peaceRequestedTurn = -1;
    private boolean attackedAfterPeaceRequest;

    private boolean missionFailed = false;
    private int lastFailedMissionTurn = -1;

    private int lastProcessedTurn = -1;

    private boolean hadTradeRouteMission = false;
    private int lastTradeTurn = -1;

    public Tribe(TribeType tribeType, HexCoordinate location, TownHall relatedTownHall) {
        this.tribeType = tribeType;
        this.tribeCamp = new TribeCamp(this);
        this.location = location;
        this.relatedTownHall = relatedTownHall;
        tribeUnits = new ArrayList<>();

        relation = 0;
        atWar = false;
        allianceActive = false;
    }

    /*
    tick function: run every turn
     */
    public void tick(int currentTurn) {
        if (currentTurn <= lastProcessedTurn) return;
        lastProcessedTurn = currentTurn;

        processPeaceRequest(currentTurn);
        updateMissionFail(currentTurn);
        processMissionDeadLine();
        if (currentMission != null) {
            currentMission.checkRequirements();
        }
        updateAllianceReward();
        reward();

        // turn-based logic
    }

    /*
    gift function
     */
    public void giftResource(Map<Resource, Integer> gift) {
        if (gift == null || isEnemy()) return;

        int relationIncrease = 0;
        relationIncrease += (gift.get(Resource.FOOD)  / 10) * 2;
        relationIncrease += (gift.get(Resource.WOOD)  / 10) * 2;
        relationIncrease += (gift.get(Resource.STONE) / 10) * 3;
        relationIncrease += (gift.get(Resource.IRON)  /  5) * 3;

        increaseRelation(relationIncrease);

        if (currentMission.getMissionState().equals(MissionState.ACTIVE)) {
            currentMission.addResource(gift);
        }
    }

    /*
    war declaration
     */
    public void warDeclaration(Happiness happiness) {
        if (happiness == null || isEnemy()) return;

        TribeMood previousMood = getMood();

        relation = MIN_RELATION;
        atWar = true;
        allianceActive = false;

        currentMission.cancelMission();
        // TODO : cancel trades

        cancelPeaceRequest();

        if (previousMood == TribeMood.ALLY) {
            happiness.addHappiness(-15);
        } else if (previousMood == TribeMood.NOT_BAD) {
            happiness.addHappiness(-5);
        }
    }

    /*
    peace request section
     */
    public boolean requestPeace(Map<Resource, Integer> payment, int currentTurn) {
        if (
            payment == null ||
            currentTurn < 0 ||
            !isEnemy() ||
            peaceRequestActive ||
            !hasPeacePayment(payment)
        ) {
            return false;
        }

        peaceRequestActive = true;
        peaceRequestedTurn = currentTurn;
        attackedAfterPeaceRequest = false;

        return true;
    }

    private boolean hasPeacePayment(Map<Resource, Integer> payment) {
        return (
            payment.get(Resource.FOOD) >= 30 &&
            payment.get(Resource.WOOD) >= 30 &&
            payment.get(Resource.IRON) >= 30
        );
    }

    private void processPeaceRequest(int currentTurn) {
        if (!peaceRequestActive) {
            return;
        }

        if (attackedAfterPeaceRequest) {
            cancelPeaceRequest();
            return;
        }

        if (currentTurn - peaceRequestedTurn < PEACE_WAIT_TURNS) {
            return;
        }

        relation = PEACE_RELATION;
        atWar = false;
        allianceActive = false;
        peaceRequestActive = false;
        peaceRequestedTurn = -1;
    }

    public void cancelPeaceRequest() {
        peaceRequestActive = false;
        peaceRequestedTurn = -1;
        attackedAfterPeaceRequest = false;
    }

    // request utils

    public boolean canRequestPeace() {
        return isEnemy() && !peaceRequestActive;
    }

    public boolean isPeaceRequestActive() {
        return peaceRequestActive;
    }

    /*
    Will notify tribe start war
     */
    public void notifyAttacked(Happiness happiness) {
        if (peaceRequestActive) attackedAfterPeaceRequest = true;
        if (!isEnemy()) warDeclaration(happiness);
    }

    /*
    Requests an alliance.
     */
    public boolean requestAlliance(int currentTurn) {
        if (!canRequestAlliance(currentTurn)) {
            return false;
        }
        allianceActive = true;
        return true;
    }

    public boolean canRequestAlliance(int currentTurn) {
        return (
            !isEnemy()
            && relation >= 70
            && !(lastFailedMissionTurn >= 0 && currentTurn - lastFailedMissionTurn < 5)
        );
    }

    public boolean isAllianceActive() {
        return (
            allianceActive
            && !isEnemy()
            && relation >= 70
        );
    }

    /*
    Alliance activation (rewarding)
     */
    private void updateAllianceReward() {
        if (allianceActive && (isEnemy() || relation < 70)) {
            allianceActive = false;
        }
    }

    public void reward() {
        if (!allianceActive) return;
        relatedTownHall.addResources(tribeType.getRelatedReward());
    }

    public Map<Resource, Integer> getPotentialReward() {
        return tribeType.getRelatedReward();
    }

    /*
    Related to mission
     */
    public void failMission() {
        missionFailed = true;
        increaseRelation(-10);
    }

    private void updateMissionFail(int turn) {
        if (missionFailed) {
            lastFailedMissionTurn = turn;
            missionFailed = false;
        }
    }

    private void processMissionDeadLine() {
        if (lastProcessedTurn - currentMission.getAcquireTurn() > currentMission.getDeadLine()) {
            currentMission.failMission();
        }
    }

    public boolean canAcquireMission() {
        return relation >= 20 && !isEnemy();
    }

    private void resetMission() {
        boolean condition = (
            canResetMission() &&
            currentMission.getMissionState().equals(MissionState.CANCELLED) &&
            currentMission.getMissionState().equals(MissionState.FAILED) &&
            currentMission.getMissionState().equals(MissionState.COMPLETED)
        );
        if (condition) currentMission = MissionType.getMissionFromTribeType(this);
    }

    public boolean canResetMission() {
        return lastFailedMissionTurn > 5 || lastFailedMissionTurn == -1;
    }

    /*
    Getters & Setters & Adders
     */
    public void increaseRelation(int amount) {
        relation = Math.max(MIN_RELATION, Math.min(MAX_RELATION, relation + amount));

        updateAllianceReward();

        if (relation <= -50) {
            atWar = true;
            allianceActive = false;
            cancelPeaceRequest();
        }
    }

    public TribeType getTribeType() {
        return tribeType;
    }

    public HexCoordinate getLocation() {
        return location;
    }

    public int getRelation() {
        return relation;
    }

    public TribeMood getMood() {
        return TribeMood.getMoodByValue(relation);
    }

    public boolean isAtWar() {
        return atWar;
    }

    public boolean isEnemy() {
        return atWar || getMood() == TribeMood.ENEMY;
    }

    public boolean canTrade() {
        return !isEnemy() && relation >= 20;
    }

    public TownHall getRelatedTownHall() {
        return relatedTownHall;
    }

    public boolean isHadTradeRouteMission() {
        return hadTradeRouteMission;
    }

    public void setHadTradeRouteMission() {
        hadTradeRouteMission = true;
    }

    public boolean hasTradedThisTurn(int turn) {
        return turn >= 0 && lastTradeTurn == turn;
    }

    public void markTrade(int turn) {
        if (turn >= 0) {
            lastTradeTurn = turn;
        }
    }

    public Mission getCurrentMission() {
        return currentMission;
    }

    public TribeCamp getTribeCamp() {
        return tribeCamp;
    }

    public List<Unit> getTribeUnits() {
        return new ArrayList<>(tribeUnits);
    }

    private void addUnit(Unit unit) {
        tribeUnits.add(unit);
    }
}