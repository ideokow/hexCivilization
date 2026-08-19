package model.game.tribe;

import controller.game.system.MovementSystem;
import model.game.building.TribeCamp;
import model.game.happiness.Happiness;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.Resource;
import model.game.townhall.TownHall;
import model.game.tribe.mission.Mission;
import model.game.tribe.mission.MissionState;
import model.game.tribe.mission.MissionType;
import model.game.unit.Unit;
import model.game.unit.military.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Tribe {

    private static final int MIN_RELATION = -100;
    private static final int MAX_RELATION =  100;

    private static final int PEACE_RELATION = -10;
    private static final int PEACE_WAIT_TURNS = 3;

    private static final int DANGER_ZONE_RADIUS = 2;

    private final TribeType tribeType;
    private final TribeCamp tribeCamp;
    private final HexCoordinate location;

    private final List<MilitaryUnit> tribeUnits;

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
    private boolean tradeOfferAvailable = false;

    private boolean suspicious = false;

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
    public SuspiciousStatus tick(int currentTurn, MovementSystem movementSystem, HexGrid grid) {
        if (currentTurn <= lastProcessedTurn) return null;
        lastProcessedTurn = currentTurn;

        processPeaceRequest(currentTurn);
        updateMissionFail(currentTurn);
        updateAllianceReward();

        processMissionDeadLine();
        if (currentMission != null) {
            currentMission.checkRequirements();
        }

        reward();

        performMilitaryBehaviour(movementSystem, grid);

        // suspicious index
        if      (isEnemy())  return SuspiciousStatus.IS_ENEMY;
        else if (suspicious) return SuspiciousStatus.IS_SUSPICIOUS;
        else return SuspiciousStatus.ITS_OK;
    }

    private void performMilitaryBehaviour(MovementSystem movementSystem, HexGrid grid) {
        switch (getMood()) {
            case ALLY     -> {
                offerThings(1.0);
                setSuspicious(false);
            }
            case NOT_BAD  -> {
                offerThings(0.8);
                setSuspicious(false);
            }
            case OK       -> {
                offerThings(0.4);
                setSuspicious(false);
                moveBackDefenders(movementSystem);
            }
            case NOT_GOOD -> {
                generateDefender();
                setSuspicious(true);
            }
            case ENEMY    -> {
                moveDefenders(movementSystem, grid);
            }
        }
    }

    private void offerThings(double prob) {
        if (new Random().nextDouble() > 1-prob) {
            resetMission();
            return;
        }
        if (new Random().nextDouble() > 1-prob && canTrade()) {
            tradeOfferAvailable = true;
        }
    }

    private void generateDefender() {
        if (tribeUnits.size() >= tribeType.getMilitaryCap()) return;

        MilitaryType[] possibleUnits = new MilitaryType[]{
                MilitaryType.SWORDSMAN,
                MilitaryType.ARCHER,
                MilitaryType.CAVALRY
        };

        switch (possibleUnits[random012(3, 2, 1)]) {
            case SWORDSMAN -> addUnit(new Swordsman(location, this));
            case ARCHER    -> addUnit(new Archer   (location, this));
            case CAVALRY   -> addUnit(new Cavalry  (location, this));
        }
    }

    private void moveDefenders(MovementSystem movementSystem, HexGrid grid) {
        Hex target = closestTarget(grid);

        if (target == null) {
            generateDefender();
            return;
        }

        for (MilitaryUnit unit : tribeUnits) {
            if (unit.getPosition().equals(location)) {
                movementSystem.move(unit, target.getCoordinate());
                return;
            }
        }
    }

    private void moveBackDefenders(MovementSystem movementSystem) {
        for (MilitaryUnit unit : tribeUnits) {
            if (!unit.getPosition().equals(location)) {
                movementSystem.move(unit, location);
                return;
            }
        }
    }

    private Hex closestTarget(HexGrid grid) {
        List<Hex> targets = grid.closestMilitaries(location, DANGER_ZONE_RADIUS);
        for (Hex target : targets) {
            List<Hex> neighborHexes = grid.hexesInRange(target.getCoordinate(), 1);

            boolean flag = false;
            for (Hex hex : neighborHexes) {
                if (hex.isThereMilitary()) {
                    flag = true;
                    break;
                }
            }
            if (!flag) return target;
        }
        return null;
    }

    private static int random012(double w0, double w1, double w2) {
        double roll = new Random().nextDouble() * (w0 + w1 + w2);

        if (roll < w0) {
            return 0;
        }
        if (roll < w0 + w1) {
            return 1;
        }
        return 2;
    }

    /*
    gift function
     */
    public void giftResource(Map<Resource, Integer> gift) {
        if (gift == null || isEnemy()) return;

        int relationIncrease = 0;
        relationIncrease += (gift.getOrDefault(Resource.FOOD, 0)  / 10) * 2;
        relationIncrease += (gift.getOrDefault(Resource.WOOD, 0)  / 10) * 2;
        relationIncrease += (gift.getOrDefault(Resource.STONE, 0) / 10) * 3;
        relationIncrease += (gift.getOrDefault(Resource.IRON, 0)  /  5) * 3;

        increaseRelation(relationIncrease);

        if (currentMission != null
                && currentMission.getMissionState().equals(MissionState.ACTIVE)) {
            currentMission.addResource(gift);
        }
    }

    /*
    war declaration
     */
    public void warDeclaration() {
        Happiness happiness = getRelatedTownHall().getHappiness();
        if (happiness == null || isEnemy()) return;

        TribeMood previousMood = getMood();

        relation = MIN_RELATION;
        atWar = true;
        allianceActive = false;

        if (currentMission != null) {
            currentMission.cancelMission();
        }
        cancelTrades();

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
    public void requestPeace(Map<Resource, Integer> payment, int currentTurn) {
        if (!canRequestPeace(payment, currentTurn)) {
            return;
        }

        peaceRequestActive = true;
        peaceRequestedTurn = currentTurn;
        attackedAfterPeaceRequest = false;
    }

    public boolean canRequestPeace(Map<Resource, Integer> payment, int currentTurn) {
        return (
                payment != null &&
                currentTurn >= 0 &&
                isEnemy() &&
                !peaceRequestActive &&
                hasPeacePayment(payment)
        );
    }

    private boolean hasPeacePayment(Map<Resource, Integer> payment) {
        return (
            payment.getOrDefault(Resource.FOOD, 0) >= 30 &&
            payment.getOrDefault(Resource.WOOD, 0) >= 30 &&
            payment.getOrDefault(Resource.IRON, 0) >= 30
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

    public boolean isPeaceRequestActive() {
        return peaceRequestActive;
    }

    /*
    Will notify tribe start war
     */
    public void notifyAttacked() {
        if (peaceRequestActive) attackedAfterPeaceRequest = true;
        if (!isEnemy()) warDeclaration();
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

    public boolean isSuspicious() {
        return suspicious;
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
        if (currentMission == null) {
            return;
        }
        if (lastProcessedTurn - currentMission.getAcquireTurn() > currentMission.getDeadLine()) {
            currentMission.failMission();
        }
    }

    public boolean canAcquireMission() {
        return relation >= 20 && !isEnemy();
    }

    private void resetMission() {
        if (!canResetMission()) {
            return;
        }

        if (currentMission == null) {
            currentMission = MissionType.getMissionFromTribeType(this);
            return;
        }

        MissionState missionState = currentMission.getMissionState();
        if (
            missionState.equals(MissionState.CANCELLED) ||
            missionState.equals(MissionState.FAILED) ||
            missionState.equals(MissionState.COMPLETED)
        ) {
            currentMission = MissionType.getMissionFromTribeType(this);
        }
    }

    public boolean canResetMission() {
        return lastFailedMissionTurn > 5 || lastFailedMissionTurn == -1;
    }

    public boolean isHadTradeRouteMission() {
        return hadTradeRouteMission;
    }

    public void setHadTradeRouteMission() {
        hadTradeRouteMission = true;
    }

    /*
    Related to trade
     */
    public boolean canTrade() {
        return !isEnemy() && relation >= 20;
    }

    public Set<Resource> getTradeResources() {
        return switch (tribeType) {
            case FARMER, COASTAL -> Set.of(Resource.FOOD);
            case MOUNTAINEER -> Set.of(Resource.STONE, Resource.IRON);
            case TRADER -> Set.of(
                    Resource.WOOD,
                    Resource.STONE,
                    Resource.IRON,
                    Resource.FOOD
            );
            case FIGHTER -> Set.of();
        };
    }

    public boolean canProvideResource(Resource resource) {
        return resource != null && getTradeResources().contains(resource);
    }

    public boolean isTradeOfferAvailable() {
        return tradeOfferAvailable && canTrade();
    }

    public boolean hasTradedThisTurn(int turn) {
        return turn >= 0 && lastTradeTurn == turn;
    }

    public void markTrade(int turn) {
        if (turn >= 0) {
            lastTradeTurn = turn;
            tradeOfferAvailable = false;
        }
    }

    private void cancelTrades() {
        tradeOfferAvailable = false;
        lastTradeTurn = -1;
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

    public TownHall getRelatedTownHall() {
        return relatedTownHall;
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

    private void addUnit(MilitaryUnit unit) {
        tribeUnits.add(unit);
    }

    public void setSuspicious(boolean suspicious) {
        this.suspicious = suspicious;
    }

    /*
    a log used in mission check requirements
     */
    private int lastLostTurn = -1;

    public void updateDeath() {
        lastLostTurn = lastProcessedTurn;
    }

    public int getLastLostTurn() {
        return lastLostTurn;
    }
}
