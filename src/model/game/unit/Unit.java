package model.game.unit;

import controller.game.GameEngine;
import model.game.happiness.Era;
import model.game.hex.HexCoordinate;
import model.game.tribe.Tribe;

import java.io.Serializable;

/*
Units main model
 */
public abstract class Unit implements Serializable {

    private static int unitsN = 0;

    private final String unitID;
    private final boolean ownedByPlayer;
    private final Tribe ownerTribe;

    private HexCoordinate position;
    private int currentAP;

    private int hp;
    private final int BASE_HP;

    public Unit(HexCoordinate position) {
        this.unitID = "unit-id-" + unitsN;
        unitsN++;
        this.position = position;
        BASE_HP = getType().getHP();
        hp = BASE_HP;

        this.ownedByPlayer = true;
        ownerTribe = null;
    }

    public Unit(HexCoordinate position, Tribe ownerTribe) {
        this.unitID = "unit-id-" + unitsN;
        unitsN++;
        this.position = position;
        BASE_HP = getType().getHP();
        hp = BASE_HP;

        this.ownedByPlayer = false;
        this.ownerTribe = ownerTribe;
    }

    // --- getters and setters ---

    public String getUnitID() {
        return unitID;
    }

    public boolean isOwnedByPlayer() {
        return ownedByPlayer;
    }

    public Tribe getOwnerTribe() {
        return ownerTribe;
    }

    public abstract UnitType getType();

    public HexCoordinate getPosition() {
        return position;
    }

    public void setPosition(HexCoordinate position) {
        this.position = position;
    }

    // --- HP things ---

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getHp() {
        return hp;
    }

    public int getHpCap() {
        return BASE_HP;
    }

    // --- AP things ---

    public void zeroAP() {
        currentAP = 0;
    }

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

    public void spendAP(int amount) {
        if (amount < 0 || currentAP < amount) return;
        currentAP -= amount;
    }

    public void addAP(int amount) {
        currentAP += amount;
        if (currentAP > getType().getEachTurnAP()) resetAP();
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
