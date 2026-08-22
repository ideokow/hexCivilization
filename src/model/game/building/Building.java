package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.registry.BuildingRegistry;
import model.game.townhall.TownHall;

import java.util.Map;
import java.util.Objects;

/*
Buildings main model
 */
public abstract class Building {

    private static int buildingN = 0;

    private final String buildingID;
    private final boolean ownedByPlayer;

    private final HexCoordinate position;
    private final BuildingType type;

    private BuildingState state;
    private int unpaidUpkeepTurns;

    private int hp;
    private final static int MAXIMUM_HP_AMOUNT = 150;

    protected Building(BuildingType type, boolean ownedByPlayer, HexCoordinate position) {
        buildingID = "building-id-" + buildingN;
        buildingN++;

        this.type = Objects.requireNonNull(type, "type");
        this.ownedByPlayer = ownedByPlayer;
        this.state = BuildingState.ACTIVE;
        this.position = position;
        hp = MAXIMUM_HP_AMOUNT;
    }

    public String getBuildingID() {
        return buildingID;
    }

    public HexCoordinate getPosition() {
        return position;
    }

    public BuildingType getType() {
        return type;
    }

    public boolean isOwnedByPlayer() {
        return ownedByPlayer;
    }

    public int getHp() {
        return hp;
    }

    public void addHp(int amount) {
        hp = Math.max(0, Math.min(MAXIMUM_HP_AMOUNT, hp + amount));
        if (hp == 0) ruin();
    }

    // --- upkeep system ---

    public boolean payUpkeep(TownHall townHall) {
        if (isRuined()) {
            return false;
        }

        Map<Resource, Integer> cost = type.getUpkeepCost();

        if (townHall.canAfford(cost)) {
            townHall.spendResources(cost);
            resetUnpaidUpkeepTurns();
            return true;
        } else {
            increaseUnpaidUpkeepTurns();
            return false;
        }
    }

    public int getUnpaidUpkeepTurns() {
        return unpaidUpkeepTurns;
    }

    public void increaseUnpaidUpkeepTurns() {
        unpaidUpkeepTurns++;
        if (unpaidUpkeepTurns == 3) {
            state = BuildingState.RUINED;
        }
    }

    public void resetUnpaidUpkeepTurns() {
        unpaidUpkeepTurns = 0;
    }

    public void setStateRuined() {
        this.state = BuildingState.RUINED;
    }

    public boolean isRuined() {
        return state == BuildingState.RUINED;
    }

    public void ruin() {
        BuildingRegistry.getInstance().ruinBuilding(this);
    }

    // ---------

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Building)) return false;
        return ((Building) obj).getBuildingID().equals(buildingID);
    }
}
