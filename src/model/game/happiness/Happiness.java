package model.game.happiness;

public class Happiness {

    private int value;

    public Happiness() {
        value = 0;
    }

    public void addHappiness(int amount) {
        value += amount;
    }

    public Era getEra() {
        if (value >= 3) {
            return Era.GOLDEN_ERA;
        }
        else if (value >= -2) {
            return Era.NORMAL_ERA;
        }
        else if (value >= -4) {
            return Era.DISCONTENT_ERA;
        }
        else {
            return Era.REBELLION_ERA;
        }
    }

    public int getValue() {
        return value;
    }
}
