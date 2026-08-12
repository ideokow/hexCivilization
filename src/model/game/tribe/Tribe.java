package model.game.tribe;

import model.game.happiness.Happiness;
import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.townhall.TownHall;

import java.util.Map;

public class Tribe {

    private static final int MIN_RELATION = -100;
    private static final int MAX_RELATION =  100;

    private static final int PEACE_RELATION = -10;
    private static final int PEACE_WAIT_TURNS = 3;

    private final TribeType tribeType;
    private final HexCoordinate location;

    private final TownHall relatedTownHall;
    private int relation;

    private boolean atWar;
    private boolean allianceActive;

    private boolean peaceRequestActive;
    private int peaceRequestedTurn = -1;
    private boolean attackedAfterPeaceRequest;

    private int lastFailedMissionTurn = -1;
    private int lastProcessedTurn = -1;

    private boolean hadTradeRouteMission = false;

    public Tribe(TribeType tribeType, HexCoordinate location, TownHall relatedTownHall) {
        this.tribeType = tribeType;
        this.location = location;
        this.relatedTownHall = relatedTownHall;

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
        updateAllianceReward();

        /*
         * Future turn-based tribe logic can be added here:
         *
         * processMission(currentTurn);
         * processEnemyBehaviour();
         * produceGuardIfNeeded(currentTurn);
         * offerMissionIfNeeded(currentTurn);
         */
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

        // TODO: cancel mission and trade

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
            && !(lastFailedMissionTurn >= 0
                    && currentTurn - lastFailedMissionTurn < 5)
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

    public Map<Resource, Integer> getReward() {
        if (!allianceActive) return null;
        return tribeType.getRelatedReward();
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
}