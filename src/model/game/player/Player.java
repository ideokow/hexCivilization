package model.game.player;

import model.game.hex.HexCoordinate;
import model.game.hex.Resource;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Player {

    private final Map<Resource, Integer> resources;
    private final Set<HexCoordinate> territory;
    private final List<Unit> units;

    public Player() {
        resources = new EnumMap<>(Resource.class);
        territory = new HashSet<>();
        units = new ArrayList<>();
    }

    public int getResourceAmount(Resource resource) {
        return resources.getOrDefault(resource, 0);
    }

    public void addResource(Resource resource, int amount) {
        if (amount <= 0) return;
        resources.put(resource, getResourceAmount(resource) + amount);
    }

    public boolean canAfford(Map<Resource, Integer> cost) {
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            if (getResourceAmount(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public boolean spendResources(Map<Resource, Integer> cost) {
        if (!canAfford(cost)) {
            return false;
        }
        for (Map.Entry<Resource, Integer> entry : cost.entrySet()) {
            resources.put(entry.getKey(), getResourceAmount(entry.getKey()) - entry.getValue());
        }
        return true;
    }

    public Map<Resource, Integer> getResources() {
        return new EnumMap<>(resources);
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

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public boolean removeUnit(Unit unit) {
        return units.remove(unit);
    }

    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }
}
