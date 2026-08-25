package model.game.player;

import model.game.hex.HexCoordinate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/*
player model stores townHall and territory
 */
public class Player implements Serializable {

    private final Set<HexCoordinate> territory;

    public Player() {
        territory = new HashSet<>();
    }

    public void addTerritory(HexCoordinate coordinate) {
        territory.add(coordinate);
    }

    public boolean ownsTerritory(HexCoordinate coordinate) {
        return territory.contains(coordinate);
    }

    public Set<HexCoordinate> getTerritory() {
        return new HashSet<>(territory);
    }
}
