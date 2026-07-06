package model.game.building;

import model.game.hex.HexCoordinate;
import model.game.player.Player;

import java.util.Objects;

/*
Buildings main model
 */
public abstract class Building {

    private final BuildingType type;
    private final Player owner;
    private BuildingState state;
    private int unpaidUpkeepTurns;
    private final HexCoordinate position;

    protected Building(BuildingType type, Player owner, HexCoordinate position) {
        this.type = Objects.requireNonNull(type, "type");
        this.owner = owner;
        this.state = BuildingState.ACTIVE;
        this.position = position;
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

    public BuildingState getState() {
        return state;
    }

    public boolean isActive() {
        return state == BuildingState.ACTIVE;
    }

    public int getUnpaidUpkeepTurns() {
        return unpaidUpkeepTurns;
    }

    public void registerPaidUpkeep() {
        unpaidUpkeepTurns = 0;
    }

    public void registerUnpaidUpkeep() {
        unpaidUpkeepTurns++;
    }

    public void ruin() {
        state = BuildingState.RUINED;
    }

    public void repair() {
        state = BuildingState.ACTIVE;
        unpaidUpkeepTurns = 0;
    }
}
