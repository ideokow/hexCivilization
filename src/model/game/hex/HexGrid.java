package model.game.hex;

import java.util.*;

/*
Holds all hexes of the map
 */
public class HexGrid {

    private final Map<HexCoordinate, Hex> hexes;
    private final Set<HexCoordinate> discovered;

    public HexGrid(Collection<Hex> allHexes) {
        this.hexes = new HashMap<>();
        this.discovered = new HashSet<>();
        for (Hex hex : allHexes) {
            hexes.put(hex.getCoordinate(), hex);
        }
    }

    // --- basic access ---

    public Hex get(HexCoordinate coordinate) {
        return hexes.get(coordinate);
    }

    public boolean contains(HexCoordinate coordinate) {
        return hexes.containsKey(coordinate);
    }

    public List<Hex> getAllHexes() {
        return new ArrayList<>(hexes.values());
    }

    // --- queries ---

    /*
    usage: routing system and movement logic
     */
    public List<Hex> neighborsOf(HexCoordinate coordinate) {
        List<Hex> result = new ArrayList<>(6);
        for (HexCoordinate c : coordinate.getNeighbors()) {
            Hex hex = hexes.get(c);
            if (hex != null) result.add(hex);
        }
        return result;
    }

    /*
    usage: units logic, like explorer and expander and ...
     */
    public List<Hex> hexesInRange(HexCoordinate center, int radius) {
        List<Hex> result = new ArrayList<>();
        for (int q = -radius; q <= radius; q++) {
            for (int r = Math.max(-radius, -q - radius);
                 r <= Math.min(radius, -q + radius); r++) {
                Hex hex = hexes.get(new HexCoordinate(center.getQ() + q, center.getR() + r));
                if (hex != null) result.add(hex);
            }
        }
        return result;
    }

    /*
    usage: in UI, prevents loading the whole map in a one scene
     */
    public List<Hex> hexesInBounds(
            int minQ,
            int maxQ,
            int minR,
            int maxR
    ) {
        if (minQ > maxQ || minR > maxR) {
            return Collections.emptyList();
        }

        List<Hex> result = new ArrayList<>();

        for (int r = minR; r <= maxR; r++) {
            for (int q = minQ; q <= maxQ; q++) {
                Hex hex = hexes.get(new HexCoordinate(q, r));
                if (hex != null) {
                    result.add(hex);
                }
            }
        }

        return result;
    }

    /*
    usage: in tribe combat logic
     */
    public List<Hex> closestMilitaries(HexCoordinate center, int range) {
        List<Hex> hexes  = hexesInRange(center, range);
        List<Hex> target = new ArrayList<>();

        for (Hex hex : hexes) {
            if (hex.isThereMilitary()) target.add(hex);
        }

        return target;
    }

    // --- Fog of War ---

    public boolean isDiscovered(HexCoordinate coordinate) {
        return discovered.contains(coordinate);
    }

    public void discover(HexCoordinate coordinate) {
        if (hexes.containsKey(coordinate)) {
            discovered.add(coordinate);
        }
    }

    public void discoverAround(HexCoordinate center, int visionRadius) {
        for (Hex hex : hexesInRange(center, visionRadius)) {
            discovered.add(hex.getCoordinate());
        }
    }

    public List<HexCoordinate> getDiscovered() {
        return new ArrayList<>(discovered);
    }

    public static int calculateDistance(HexCoordinate hex1, HexCoordinate hex2) {
        int dq = hex1.getQ() - hex2.getQ();
        int dr = hex1.getR() - hex2.getR();
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }
}
