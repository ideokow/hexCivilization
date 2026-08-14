package model.game.unit;

import model.game.happiness.Era;
import model.game.hex.HexCoordinate;
import model.game.player.Player;

/*
Units main model
 */
public abstract class Unit {

    private static int unitsN = 0;

    private final String unitID;
    private final Player owner;
    private int currentAP;
    private HexCoordinate position;

    private int hp;
    private final static int MAXIMUM_HP_AMOUNT = 50;
    private final static int MINIMUM_HP_AMOUNT = 0;

    public Unit(Player owner, HexCoordinate position) {
        this.unitID = "unit-id-" + unitsN;
        unitsN++;
        this.owner = owner;
        this.position = position;
        hp = MAXIMUM_HP_AMOUNT;
    }

    // --- getters and setters ---

    public String getUnitID() {
        return unitID;
    }

    public Player getOwner() {
        return owner;
    }

    public abstract UnitType getType();

    public HexCoordinate getPosition() {
        return position;
    }

    public void setPosition(HexCoordinate position) {
        this.position = position;
    }

    // --- HP things ---

    public int getHp() {
        return hp;
    }

    public void increaseHp(int amount) {
        hp = Math.max(MINIMUM_HP_AMOUNT, Math.min(MAXIMUM_HP_AMOUNT, hp + amount));
    }

    // --- AP things ---

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
