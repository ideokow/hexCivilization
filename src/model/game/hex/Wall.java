package model.game.hex;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Wall {

    public static final Map<Resource, Integer> COST = Map.of(
            Resource.WOOD, 1,
            Resource.FOOD, 0,
            Resource.IRON, 0,
            Resource.STONE, 1
    );
    public static final int COST_AP = 1;

    private static final int MAXIMUM_HP = 50;

    private final HexCoordinate coordinate1;
    private final HexCoordinate coordinate2;

    private int hp;

    public Wall(HexCoordinate a, HexCoordinate b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        boolean swap = a.getQ() > b.getQ() || (a.getQ() == b.getQ() && a.getR() > b.getR());
        this.coordinate1 = swap ? b : a;
        this.coordinate2 = swap ? a : b;
        this.hp = MAXIMUM_HP;
    }

    public int getHp() {
        return hp;
    }

    void addHp(int amount) {
        hp = Math.max(0, Math.min(MAXIMUM_HP, hp + amount));
    }

    public boolean isRuined() {
        return hp == 0;
    }

    public HexCoordinate getCoordinate1() {
        return coordinate1;
    }

    public HexCoordinate getCoordinate2() {
        return coordinate2;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Wall wall)) return false;
        return coordinate1.equals(wall.coordinate1) && coordinate2.equals(wall.coordinate2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate1, coordinate2);
    }
}
