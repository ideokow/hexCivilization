package controller.game;

import model.game.building.BuildingType;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.hex.TerrainType;
import model.game.season.SeasonName;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DisasterSpawner {

    private static final Random rnd = new Random();

    private static final double EACH_TURN_PROBABILITY = 0.05;

    private static final int EARTH_QUAKE_DAMAGE_ON_BUILDING = 50;
    private static final int EARTH_QUAKE_DAMAGE_ON_UNIT     = 10;
    private static final int FLOOD_DAMAGE_ON_BUILDING       = 30;
    private static final int FLOOD_DAMAGE_ON_UNIT           = 20;
    private static final int BEAR_DAMAGE_ON_UNIT            = 20;

    private final HexGrid hexGrid;
    private final GameController gameController;

    public DisasterSpawner(HexGrid hexGrid, GameController gameController) {
        this.hexGrid = hexGrid;
        this.gameController = gameController;
    }

    private boolean isDisaster() {
        return rnd.nextDouble() < EACH_TURN_PROBABILITY;
    }

    public void tick(SeasonName season) {
        if (!isDisaster()) return;

        List<Disaster> disasters = new ArrayList<>();
        disasters.add(Disaster.EARTH_QUAKE);
        disasters.add(Disaster.BEAR_ATTACK);
        if (season.equals(SeasonName.FALL)) disasters.add(Disaster.FLOOD);

        // select disaster
        Disaster chosenDisaster = disasters.get(rnd.nextInt(disasters.size()));

        // make disaster!
        switch (chosenDisaster) {
            case EARTH_QUAKE -> earthQuake();
            case FLOOD       -> flood();
            case BEAR_ATTACK -> bearAttack();
        }
    }

    // --- earthquake ---

    private void earthQuake() {
        HexCoordinate center = selectEarthquakeHex();
        if (center == null) return;

        List<Hex> hexes = hexGrid.hexesInRange(center, 2);
        effectEarthQuake(hexes);

        gameController.toastAlert(Disaster.EARTH_QUAKE.getMessage());

        // TODO : animate earth quake, apply on `hexes` list
    }

    private void effectEarthQuake(List<Hex> hexes) {
        for (Hex hex : new ArrayList<>(hexes)) {
            if (!hex.getTerrain().isLand()) {
                hexes.remove(hex);
                continue;
            }

            if (hex.getBuilding() != null) {
                int damage = EARTH_QUAKE_DAMAGE_ON_BUILDING;

                if (hex.getBuilding().getType().equals(BuildingType.TOWN_HALL)) {
                    damage = Math.max(0, Math.min(hex.getBuilding().getHp()-1, EARTH_QUAKE_DAMAGE_ON_BUILDING));
                }
                hex.getBuilding().increaseHp(-damage);
            }

            for (Unit unit : new ArrayList<>(hex.getUnits())) {
                unit.increaseHp(-EARTH_QUAKE_DAMAGE_ON_UNIT);
            }
        }
    }

    private HexCoordinate selectEarthquakeHex() {
        List<HexCoordinate> discoveredArea = hexGrid.getDiscovered();
        List<HexCoordinate> toDelete = new ArrayList<>();

        for (HexCoordinate coordinate : discoveredArea) {
            if (!hexGrid.get(coordinate).getTerrain().isLand()) toDelete.add(coordinate);
        }

        for (HexCoordinate coordinate : toDelete) {
            discoveredArea.remove(coordinate);
        }

        int size = discoveredArea.size();
        if (size == 0) return null;

        int index = rnd.nextInt(size);
        return discoveredArea.get(index);
    }

    // --- flood ---

    private void flood() {
        HexCoordinate center = selectFloodHex();
        if (center == null) return;

        List<Hex> hexes = hexGrid.hexesInRange(center, 1);
        effectFlood(hexes);

        gameController.toastAlert(Disaster.FLOOD.getMessage());

        // TODO : animate flood, apply on `hexes` list
    }

    private void effectFlood(List<Hex> hexes) {
        for (Hex hex : new ArrayList<>(hexes)) {
            if (hex.getTerrain().equals(TerrainType.MOUNTAIN)
                    || hex.getTerrain().equals(TerrainType.MOUNTAIN_RANGE)
                    || !hex.getTerrain().isLand()) {
                hexes.remove(hex);
                continue;
            }

            if (hex.getBuilding() != null) {
                int damage = FLOOD_DAMAGE_ON_BUILDING;
                if (hex.getBuilding().getType().equals(BuildingType.TOWN_HALL)) {
                    damage = Math.max(0, Math.min(hex.getBuilding().getHp()-1, FLOOD_DAMAGE_ON_BUILDING));
                }
                hex.getBuilding().increaseHp(-damage);
            }

            for (Unit unit : new ArrayList<>(hex.getUnits())) {
                unit.increaseHp(-FLOOD_DAMAGE_ON_UNIT);
                unit.zeroAP();
            }
        }
    }

    private HexCoordinate selectFloodHex() {
        List<HexCoordinate> discoveredArea = hexGrid.getDiscovered();
        List<HexCoordinate> toDelete = new ArrayList<>();

        for (HexCoordinate coordinate : discoveredArea) {
            if (!hasWaterInNeighbor(coordinate)
                    || !hexGrid.get(coordinate).getTerrain().isLand()
                    || hexGrid.get(coordinate).getTerrain().equals(TerrainType.MOUNTAIN)
                    || hexGrid.get(coordinate).getTerrain().equals(TerrainType.MOUNTAIN_RANGE)
            ) toDelete.add(coordinate);
        }

        for (HexCoordinate coordinate : toDelete) {
            discoveredArea.remove(coordinate);
        }

        int size = discoveredArea.size();
        if (size == 0) return null;

        int index = rnd.nextInt(size);
        return discoveredArea.get(index);
    }

    private boolean hasWaterInNeighbor(HexCoordinate select) {
        List<Hex> neighbors = hexGrid.hexesInRange(select, 1);
        for (Hex hex : neighbors) {
            if (!hex.getTerrain().isLand()) return true;
        }
        return false;
    }

    // --- bear attack ---

    private void bearAttack() {
        HexCoordinate attackHex = selectBearAttackHex();
        if (attackHex == null) return;

        effectBearAttack(hexGrid.get(attackHex));

        gameController.toastAlert(Disaster.BEAR_ATTACK.getMessage());

        // TODO : animate bear attack, apply on `attackHex` hex
    }

    private void effectBearAttack(Hex hex) {
        for (Unit unit : new ArrayList<>(hex.getUnits())) {
            if (unit.isOwnedByPlayer()) unit.increaseHp(-BEAR_DAMAGE_ON_UNIT);
        }
    }

    private HexCoordinate selectBearAttackHex() {
        List<HexCoordinate> discoveredArea = hexGrid.getDiscovered();
        List<HexCoordinate> toDelete = new ArrayList<>();

        for (HexCoordinate coordinate : discoveredArea) {
            if (!hexGrid.get(coordinate).getTerrain().isLand() || !hasOwnedUnit(coordinate)) toDelete.add(coordinate);
        }

        for (HexCoordinate coordinate : toDelete) {
            discoveredArea.remove(coordinate);
        }

        int size = discoveredArea.size();
        if (size == 0) return null;

        int index = rnd.nextInt(size);
        return discoveredArea.get(index);
    }

    private boolean hasOwnedUnit(HexCoordinate select) {
        for (Unit unit : hexGrid.get(select).getUnits()) {
            if (unit.isOwnedByPlayer()) return true;
        }
        return false;
    }
}
