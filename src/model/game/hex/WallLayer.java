package model.game.hex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WallLayer {

    private final Set<Wall> walls = new HashSet<>();

    public void addWall(Wall wall) {
        if (wall == null) return;
        if (wall.getCoordinate1().distanceTo(wall.getCoordinate2()) != 1) return;
        walls.add(wall);
    }

    public void removeWall(Wall wall) {
        walls.remove(wall);
    }

    public void addHp(Wall wall, int amount) {
        wall.addHp(amount);
        if (wall.isRuined()) removeWall(wall);
    }

    // --- getters ---

    public boolean isThereWall(HexCoordinate a, HexCoordinate b) {
        return walls.contains(new Wall(a, b));
    }

    public List<Wall> getWalls(HexCoordinate hexCoordinate) {
        List<Wall> result = new ArrayList<>(6);
        for (Wall wall : walls) {
            if (wall.getCoordinate1().equals(hexCoordinate)
                    || wall.getCoordinate2().equals(hexCoordinate)) {
                result.add(wall);
            }
        }
        return result;
    }

    public Set<Wall> getAllWalls() {
        return new HashSet<>(walls);
    }

    private Wall getWall(HexCoordinate a, HexCoordinate b) {
        Wall key = new Wall(a, b);
        for (Wall wall : walls) {
            if (wall.equals(key)) return wall;
        }
        return null;
    }
}
