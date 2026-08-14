package model.game.tribe.mission;

import model.game.building.BuildingType;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.tribe.Tribe;

import java.util.List;

public class CoastalMission extends Mission {

    private final static int DOCK_RADIUS = 4;

    public CoastalMission(Tribe tribe) {
        super(tribe, MissionType.COASTAL_DEVELOPMENT);
    }

    @Override
    public void payReward() {
        getTribe().getRelatedTownHall().addFoodToStorage(30);
        getTribe().getRelatedTownHall().giveDockBuildingBonus();
    }

    @Override
    public boolean checkRequirements() {
        if (checkIsThereDockNearby(
            getTribe().getRelatedTownHall().getGrid(),
            getTribe().getLocation()
        )) {
            setIsReady();
            return true;
        }
        return false;
    }

    private static boolean checkIsThereDockNearby(HexGrid hexGrid, HexCoordinate coordinate) {
        List<Hex> hexesInRange = hexGrid.hexesInRange(coordinate, DOCK_RADIUS);
        for (Hex hex: hexesInRange) {
            if (hex.getBuilding().getType().equals(BuildingType.DOCK)) {
                return true;
            }
        }
        return false;
    }
}
