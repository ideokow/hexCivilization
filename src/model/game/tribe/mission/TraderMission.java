package model.game.tribe.mission;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import model.game.building.BuildingType;
import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.tribe.Tribe;

public class TraderMission extends Mission {

    public TraderMission(Tribe tribe) {
        super(tribe, MissionType.TRADE_ROUTE);
    }

    @Override
    public void payReward() {
        getTribe().increaseRelation(20);
        getTribe().setHadTradeRouteMission();
    }

    @Override
    public boolean checkRequirements() {
        return checkIsThereRoadToAnyNearbyBuilding(
                getTribe().getRelatedTownHall().getGrid(),
                getTribe().getLocation()
        );
    }

    private static boolean checkIsThereRoadToAnyNearbyBuilding(HexGrid hexGrid, HexCoordinate start) {
        Hex startHex = hexGrid.get(start);
        if (startHex == null) return false;

        Set<HexCoordinate> visited = new HashSet<>();
        Queue<HexCoordinate> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Hex current = hexGrid.get(queue.poll());
            if (current == null) continue;

            if (current.getBuilding() != null
                    && !current.getBuilding().getType().equals(BuildingType.ROAD)) {
                // TODO : check that build is not tribe camp
                return true;
            }

            if (!current.isThereRoad() && !current.getCoordinate().equals(start)) {
                continue;
            }

            for (Hex neighbor : hexGrid.neighborsOf(current.getCoordinate())) {
                if (!visited.contains(neighbor.getCoordinate())) {
                    visited.add(neighbor.getCoordinate());
                    queue.add(neighbor.getCoordinate());
                }
            }
        }

        return false;
    }
}
