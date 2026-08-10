package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;
import model.game.townhall.TownHall;

import java.util.Objects;

/*
Buildings main model
 */
public abstract class Building {

    private static int buildingN = 0;

    private final String buildingID;
    private final BuildingType type;
    private final Player owner;
    private BuildingState state;
    private int unpaidUpkeepTurns;
    private final HexCoordinate position;

    protected Building(BuildingType type, Player owner, HexCoordinate position) {

        buildingID = "building-id-" + buildingN;
        buildingN++;

        this.type = Objects.requireNonNull(type, "type");
        this.owner = owner;
        this.state = BuildingState.ACTIVE;
        this.position = position;
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

    public Player getOwner() {
        return owner;
    }

    // --- upkeep system ---

    public abstract boolean payUpkeep(TownHall townHall);

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

    public boolean isRuined() {
        return state == BuildingState.RUINED;
    }

    // ---------

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Building)) return false;
        return ((Building) obj).getBuildingID().equals(buildingID);
    }
}
