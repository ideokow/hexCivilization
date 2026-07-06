package model.game.hex;

import model.game.building.Building;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
Hexes main model
 */
public class Hex {

    private final HexCoordinate coordinate;
    private final TerrainType terrain;
    private final HexResource resource;

    private Building building;
    private final List<Unit> units;

    public Hex(HexCoordinate coordination, TerrainType terrainType, HexResource resourceType) {
        this.coordinate = coordination;
        resource = resourceType;
        terrain = terrainType;
        units = new ArrayList<>();
    }

    // Hex Main parameters

    public HexCoordinate getCoordinate() {
        return coordinate;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public HexResource getResource() {
        return resource;
    }

    // building

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    // units

    public List<Unit> getUnits() {
        return new ArrayList<>(units);
    }

    public void addUnit(Unit unit) {
        units.add(Objects.requireNonNull(unit, "unit"));
    }

    public boolean removeUnit(Unit unit) {
        return units.remove(unit);
    }

    // overrides


    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Hex)) return false;
        return this.coordinate.equals(((Hex) obj).getCoordinate());
    }
}