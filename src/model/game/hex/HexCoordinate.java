package model.game.hex;

import java.util.ArrayList;
import java.util.List;

/*
 * AXIAL coordination implementation
 */
public class HexCoordinate {

    private static final int[][] DIRECTIONS = {
            { 1,  0}, {-1,  0},
            { 0,  1}, { 0, -1},
            { 1, -1}, {-1,  1}
    };

    // AXIAL coordination
    private final int q;
    private final int r;

    public HexCoordinate(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    public boolean isNeighbor(HexCoordinate destinationHex) {
        int dq = destinationHex.getQ() - this.q;
        int dr = destinationHex.getR() - this.r;

        for (int[] dir : DIRECTIONS) {
            if (dir[0] == dq && dir[1] == dr) return true;
        }
        return false;
    }

    public List<HexCoordinate> getNeighbors() {
        List<HexCoordinate> neighbors = new ArrayList<>();
        for (int[] dir : DIRECTIONS) {
            neighbors.add(new HexCoordinate(q + dir[0], r + dir[1]));
        }
        return neighbors;
    }

    public int distanceTo(HexCoordinate other) {
        int dq = other.getQ() - this.q;
        int dr = other.getR() - this.r;
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexCoordinate)) return false;
        return q == ((HexCoordinate) o).q && r == ((HexCoordinate) o).r;
    }
}

// check here:
// https://www.redblobgames.com/grids/hexagons/#:~:text=Axial%20coordinates%23&text=The%20axial%2Fcube%20system%20allows,simpler%20with%20axial%2Fcube%20coordinates.