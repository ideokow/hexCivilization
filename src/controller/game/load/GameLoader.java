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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLoader {

    private HexGrid hexGrid;
    private Player player;
    private BuildingMap buildingMap;
    private UnitMap unitMap;
    private TownHall townHall;
    private List<Tribe> tribes;
    private Map<Route, Unit> inQueueRoutes;
    private int turnNumber;

    public GameLoader() {}

    public boolean checkSave() {
        // TODO: implement check save
        return false;
    }

    public void loadGame(int MAP_NUMBER) {
        GameLoader gameLoader = new GameLoader();
        boolean save = gameLoader.checkSave();

        if (save) {
            // TODO: implement load from save
        } else {
            MapLoader mapLoader = new MapLoader();

            // hexGrid
            try {
                hexGrid = (new MapLoader()).loadMap(MAP_NUMBER);

                // player
                player = new Player();
                for (HexCoordinate hexCoordinate : hexGrid.getDiscovered()) {
                    player.addTerritory(hexCoordinate);
                }

                // unit and building maps
                buildingMap = new BuildingMap(hexGrid);
                unitMap = new UnitMap(hexGrid);

                // town hall
                townHall = new TownHall(hexGrid, unitMap);
                hexGrid.get(new HexCoordinate(0, 0)).setBuilding(townHall);

                buildingMap.setTownHall(townHall);
                buildingMap.addBuilding(townHall);
                unitMap.setTownHall(townHall);

                // load tribes
                tribes = mapLoader.loadTribes(MAP_NUMBER, townHall);
                mapLoader.loadTribesInGrid(hexGrid, tribes);

                // trading posts
                List<HexCoordinate> tradingPosts = mapLoader.loadTradingPosts(MAP_NUMBER);
                mapLoader.loadTradingPostsInGrid(hexGrid, tradingPosts);

                // rout queue
                inQueueRoutes = new HashMap<>();

                // current turn
                turnNumber = 1;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
       // TODO : implement save
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
