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

    private static final boolean DEBUG_VERBOSE = false;

    private static final Random rnd = new Random();

    private static final double EACH_TURN_PROBABILITY = 0.05;

    private static final int EARTH_QUAKE_DAMAGE_ON_BUILDING = 50;
    private static final int EARTH_QUAKE_DAMAGE_ON_UNIT     = 10;
    private static final int FLOOD_DAMAGE_ON_BUILDING       = 30;
    private static final int FLOOD_DAMAGE_ON_UNIT           = 20;
    private static final int BEAR_DAMAGE_ON_UNIT            = 20;

    private GameController gameController;

    public DisasterSpawner() {}

    void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    private boolean isDisaster() {
        return rnd.nextDouble() < EACH_TURN_PROBABILITY;
    }

    public void tick(HexGrid hexGrid, UnitMap unitMap, BuildingMap buildingMap, SeasonName season) {
        if (!isDisaster()) return;

        List<Disaster> disasters = new ArrayList<>();
        disasters.add(Disaster.EARTH_QUAKE);
        disasters.add(Disaster.BEAR_ATTACK);
        if (season.equals(SeasonName.FALL)) disasters.add(Disaster.FLOOD);

        // select disaster
        Disaster chosenDisaster = disasters.get(rnd.nextInt(disasters.size()));

        if (DEBUG_VERBOSE) System.out.println("[INFO]: disaster selected: " + chosenDisaster.toString());

        // make disaster!
        switch (chosenDisaster) {
            case EARTH_QUAKE -> earthQuake(hexGrid, unitMap, buildingMap);
            case FLOOD       -> flood     (hexGrid, unitMap, buildingMap);
            case BEAR_ATTACK -> bearAttack(hexGrid, unitMap);
        }
    }

    // --- earthquake ---

    private void earthQuake(HexGrid hexGrid, UnitMap unitMap, BuildingMap buildingMap) {
        HexCoordinate center = selectEarthquakeHex(hexGrid);
        if (center == null) return;

        List<Hex> hexes = hexGrid.hexesInRange(center, 2);
        effectEarthQuake(hexes, unitMap, buildingMap);

        gameController.toastAlert(Disaster.EARTH_QUAKE.getMessage());
        gameController.animateDisaster(
                Disaster.EARTH_QUAKE,
                coordinatesOf(hexes)
        );
    }

    private void effectEarthQuake(List<Hex> hexes, UnitMap unitMap, BuildingMap buildingMap) {
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
                buildingMap.changeHp(hex.getBuilding(), -damage);
            }

            for (Unit unit : new ArrayList<>(hex.getUnits())) {
                unitMap.changeHp(unit, -EARTH_QUAKE_DAMAGE_ON_UNIT);
            }
        }
    }

    private HexCoordinate selectEarthquakeHex(HexGrid hexGrid) {
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

    private void flood(HexGrid hexGrid, UnitMap unitMap, BuildingMap buildingMap) {
        HexCoordinate center = selectFloodHex(hexGrid);
        if (center == null) return;

        List<Hex> hexes = hexGrid.hexesInRange(center, 1);
        effectFlood(hexes, unitMap, buildingMap);

        gameController.toastAlert(Disaster.FLOOD.getMessage());
        gameController.animateDisaster(
                Disaster.FLOOD,
                coordinatesOf(hexes)
        );
    }

    private void effectFlood(List<Hex> hexes, UnitMap unitMap, BuildingMap buildingMap) {
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
                buildingMap.changeHp(hex.getBuilding(), -damage);
            }

            for (Unit unit : new ArrayList<>(hex.getUnits())) {
                unitMap.changeHp(unit, -FLOOD_DAMAGE_ON_UNIT);
                unit.zeroAP();
            }
        }
    }

    private HexCoordinate selectFloodHex(HexGrid hexGrid) {
        List<HexCoordinate> discoveredArea = hexGrid.getDiscovered();
        List<HexCoordinate> toDelete = new ArrayList<>();

        for (HexCoordinate coordinate : discoveredArea) {
            if (!hasWaterInNeighbor(hexGrid, coordinate)
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

    private boolean hasWaterInNeighbor(HexGrid hexGrid, HexCoordinate select) {
        List<Hex> neighbors = hexGrid.hexesInRange(select, 1);
        for (Hex hex : neighbors) {
            if (!hex.getTerrain().isLand()) return true;
        }
        return false;
    }

    // --- bear attack ---

    private void bearAttack(HexGrid hexGrid, UnitMap unitMap) {
        HexCoordinate attackHex = selectBearAttackHex(hexGrid);
        if (attackHex == null) return;

        effectBearAttack(hexGrid.get(attackHex), unitMap);

        gameController.toastAlert(Disaster.BEAR_ATTACK.getMessage());
        gameController.animateDisaster(
                Disaster.BEAR_ATTACK,
                List.of(attackHex)
        );
    }

    private void effectBearAttack(Hex hex, UnitMap unitMap) {
        for (Unit unit : new ArrayList<>(hex.getUnits())) {
            if (unit.isOwnedByPlayer()) unitMap.changeHp(unit, -BEAR_DAMAGE_ON_UNIT);
        }
    }

    private HexCoordinate selectBearAttackHex(HexGrid hexGrid) {
        List<HexCoordinate> discoveredArea = hexGrid.getDiscovered();
        List<HexCoordinate> toDelete = new ArrayList<>();

        for (HexCoordinate coordinate : discoveredArea) {
            if (!hexGrid.get(coordinate).getTerrain().isLand()
                    || !hasOwnedUnit(hexGrid, coordinate)
                    || hexGrid.get(coordinate).getTerrain().equals(TerrainType.TOWN_HALL)
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

    private boolean hasOwnedUnit(HexGrid hexGrid, HexCoordinate select) {
        for (Unit unit : hexGrid.get(select).getUnits()) {
            if (unit.isOwnedByPlayer()) return true;
        }
        return false;
    }

    private List<HexCoordinate> coordinatesOf(List<Hex> hexes) {
        return hexes.stream()
                .map(Hex::getCoordinate)
                .toList();
    }
}
