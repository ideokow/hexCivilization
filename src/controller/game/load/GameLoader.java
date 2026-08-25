package controller.game.load;

import controller.game.map.BuildingMap;
import controller.game.map.UnitMap;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.player.Player;
import model.game.route.Route;
import model.game.townhall.TownHall;
import model.game.tribe.Tribe;
import model.game.unit.Unit;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLoader implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String SAVE_ADDRESS = "./save/";
    private static final String SAVE_FILE = "game.save";

    private HexGrid hexGrid;
    private Player player;
    private BuildingMap buildingMap;
    private UnitMap unitMap;
    private TownHall townHall;
    private List<Tribe> tribes;
    private Map<Route, Unit> inQueueRoutes;
    private int turnNumber = -1;

    public GameLoader() {}

    public boolean checkSave() {
        Path savePath = getSavePath();

        if (!Files.isRegularFile(savePath)) {
            return false;
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(savePath.toFile()))) {
            Object savedGame = objectInputStream.readObject();
            if (!(savedGame instanceof GameLoader && objectInputStream.read() == -1))
                return false;
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            return false;
        }

        if (hexGrid == null) return false;
        if (player == null) return false;
        if (buildingMap == null) return false;
        if (unitMap == null) return false;
        if (townHall == null) return false;
        if (tribes == null) return false;
        if (inQueueRoutes == null) return false;
        if (turnNumber <= 0) return false;

        return true;
    }

    public void loadGame(int MAP_NUMBER) {
        if (checkSave()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(getSavePath().toFile()))) {
                GameLoader savedGame = (GameLoader) objectInputStream.readObject();
                copyStateFrom(savedGame);
                return;
            } catch (IOException | ClassNotFoundException | RuntimeException e) {
                startNewGame(MAP_NUMBER);
                return;
            }
        }

        startNewGame(MAP_NUMBER);
    }

    public void saveGame(
            HexGrid hexGrid,
            Player player,
            BuildingMap buildingMap,
            UnitMap unitMap,
            TownHall townHall,
            List<Tribe> tribes,
            Map<Route, Unit> inQueueRoutes,
            int turnNumber
    ) {
        this.hexGrid = hexGrid;
        this.player = player;
        this.buildingMap = buildingMap;
        this.unitMap = unitMap;
        this.townHall = townHall;
        this.tribes = tribes;
        this.inQueueRoutes = inQueueRoutes;
        this.turnNumber = turnNumber;

        Path saveDirectory = Paths.get(SAVE_ADDRESS);
        Path savePath = getSavePath();
        Path temporarySavePath = null;

        try {
            Files.createDirectories(saveDirectory);
            temporarySavePath = Files.createTempFile(saveDirectory, SAVE_FILE, ".tmp");

            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    new FileOutputStream(temporarySavePath.toFile()))) {
                objectOutputStream.writeObject(this);
                objectOutputStream.flush();
            }

            try {
                Files.move(temporarySavePath, savePath,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporarySavePath, savePath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            if (temporarySavePath != null) {
                try {
                    Files.deleteIfExists(temporarySavePath);
                } catch (IOException ignored) {
                }
            }
            e.printStackTrace();
        }
    }

    private Path getSavePath() {
        return Paths.get(SAVE_ADDRESS, SAVE_FILE);
    }

    private void copyStateFrom(GameLoader savedGame) {
        hexGrid = savedGame.hexGrid;
        player = savedGame.player;
        buildingMap = savedGame.buildingMap;
        unitMap = savedGame.unitMap;
        townHall = savedGame.townHall;
        tribes = savedGame.tribes;
        inQueueRoutes = savedGame.inQueueRoutes;
        turnNumber = savedGame.turnNumber;
    }

    private void startNewGame(int MAP_NUMBER) {
        MapLoader mapLoader = new MapLoader();

        try {
            hexGrid = mapLoader.loadMap(MAP_NUMBER);

            player = new Player();
            for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
                player.addTerritory(hexCoordinate);
            }

            buildingMap = new BuildingMap(hexGrid);
            unitMap = new UnitMap(hexGrid);

            townHall = new TownHall(hexGrid, unitMap);
            hexGrid.get(new HexCoordinate(0, 0)).setBuilding(townHall);

            buildingMap.setTownHall(townHall);
            buildingMap.addBuilding(townHall);
            unitMap.setTownHall(townHall);

            tribes = mapLoader.loadTribes(MAP_NUMBER, townHall);
            mapLoader.loadTribesInGrid(hexGrid, tribes);

            List<HexCoordinate> tradingPosts = mapLoader.loadTradingPosts(MAP_NUMBER);
            mapLoader.loadTradingPostsInGrid(hexGrid, tradingPosts);

            inQueueRoutes = new HashMap<>();
            turnNumber = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HexGrid getHexGrid() {
        return hexGrid;
    }

    public Player getPlayer() {
        return player;
    }

    public BuildingMap getBuildingMap() {
        return buildingMap;
    }

    public UnitMap getUnitMap() {
        return unitMap;
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public List<Tribe> getTribes() {
        return tribes;
    }

    public Map<Route, Unit> getInQueueRoutes() {
        return inQueueRoutes;
    }

    public int getTurnNumber() {
        return turnNumber;
    }
}
