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

    public List<Hex> neighborsOf(HexCoordinate coordinate) {
        List<Hex> result = new ArrayList<>(6);
        for (HexCoordinate c : coordinate.getNeighbors()) {
            Hex hex = hexes.get(c);
            if (hex != null) result.add(hex);
        }
        return result;
    }

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

    public Set<HexCoordinate> getDiscovered() {
        return new HashSet<>(discovered);
    }
}
