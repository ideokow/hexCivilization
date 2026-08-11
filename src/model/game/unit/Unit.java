package model.game.unit;

import model.game.happiness.Era;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.player.Player;

import java.util.Objects;

/*
Units main model
 */
public abstract class Unit {

    private static int unitsN = 0;

    private final String unitID;
    private final Player owner;
    private int currentAP;
    private HexCoordinate position;

    public Unit(Player owner, HexCoordinate position) {

        this.unitID = "unit-id-" + unitsN;
        this.owner = owner;
        this.position = position;

        unitsN++;
    }

    public String getUnitID() {
        return unitID;
    }

    public Player getOwner() {
        return owner;
    }

    public abstract UnitType getType();

    public void resetAP() {
        currentAP = getType().getEachTurnAP();
    }

    public void resetAP(Era era) {
        resetAP();
        if (era.equals(Era.REBELLION_ERA)) {
            currentAP -= 1;
        }
    }

    public int getCurrentAP() {
        return currentAP;
    }

    public boolean spendAP(int amount) {
        if (amount < 0 || currentAP < amount) {
            return false;
        }
        currentAP -= amount;
        return true;
    }

    public void addAP(int amount) {
        currentAP += amount;
        if (currentAP > getType().getEachTurnAP()) resetAP();
    }

    public HexCoordinate getPosition() {
        return position;
    }

    public void setPosition(HexCoordinate position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        return unitID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Unit)) return false;
        return unitID.equals(((Unit) obj).unitID);
    }
}
