package model.game.tribe;

public enum TribeMood {
    ENEMY(-2),
    NOT_GOOD(-1),
    OK(0),
    NOT_BAD(1),
    ALLY(2);

    private final int value; // for comparing

    TribeMood(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TribeMood getMoodByValue(int relation) {
        if (relation >= 70) {
            return TribeMood.ALLY;
        }
        else if (relation >= 20) {
            return TribeMood.NOT_BAD;
        }
        else if (relation >= -19) {
            return TribeMood.OK;
        }
        else if (relation >= -49){
            return TribeMood.NOT_GOOD;
        }
        else {
            return TribeMood.ENEMY;
        }
    }
}
