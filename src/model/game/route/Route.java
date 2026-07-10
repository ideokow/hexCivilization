package model.game.route;

import model.game.hex.Hex;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;

import java.util.*;

public class Route {

    private static int routesN = 0;

    private final String routeID;
    private final HexCoordinate origin;
    private final HexCoordinate destination;

    private HexCoordinate currentPosition;

    private List<HexCoordinate> steps;

    public Route(HexCoordinate origin, HexCoordinate destination, HexGrid hexGrid) {

        routeID = "route-id-" + routesN;
        routesN ++;

        this.origin = origin;
        this.destination = destination;

        generateSteps(hexGrid);
        if (steps.size() == 0) {
            throw new IllegalArgumentException("Can't find any routes!");
        }

        currentPosition = new HexCoordinate(origin.getQ(), origin.getR());
    }

    public HexCoordinate getOrigin() {
        return origin;
    }

    public HexCoordinate getDestination() {
        return destination;
    }

    public HexCoordinate getCurrentPosition() {
        return currentPosition;
    }

    // --- path generation ---

    private void generateSteps(HexGrid hexGrid) {
        steps = new ArrayList<>();

        if (origin.equals(destination)) {
            return;
        }
        if (!hexGrid.contains(origin) || !hexGrid.contains(destination)) {
            return; // no valid path
        }

        // gScore: cheapest known cost from origin to a coordinate
        Map<HexCoordinate, Integer> gScore = new HashMap<>();
        // cameFrom: to reconstruct the path
        Map<HexCoordinate, HexCoordinate> cameFrom = new HashMap<>();

        // Priority queue ordered by fScore = gScore + heuristic
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.fScore));

        gScore.put(origin, 0);
        open.add(new Node(origin, heuristic(origin)));

        Set<HexCoordinate> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node current = open.poll();
            HexCoordinate currentCoord = current.coordinate;

            if (currentCoord.equals(destination)) {
                reconstructPath(cameFrom, currentCoord);
                return;
            }

            if (!closed.add(currentCoord)) {
                continue; // already processed with a better/equal score
            }

            int currentG = gScore.get(currentCoord);

            for (Hex neighborHex : hexGrid.neighborsOf(currentCoord)) {
                HexCoordinate neighbor = neighborHex.getCoordinate();
                if (closed.contains(neighbor)) {
                    continue;
                }

                int enterCost = neighborHex.getTerrain().getMovementCost();
                int tentativeG = currentG + enterCost;

                Integer knownG = gScore.get(neighbor);
                if (knownG == null || tentativeG < knownG) {
                    gScore.put(neighbor, tentativeG);
                    cameFrom.put(neighbor, currentCoord);
                    open.add(new Node(neighbor, tentativeG + heuristic(neighbor)));
                }
            }
        }
    }

    private int heuristic(HexCoordinate from) {
        return from.distanceTo(destination);
    }

    private void reconstructPath(Map<HexCoordinate, HexCoordinate> cameFrom,
                                 HexCoordinate current) {
        LinkedList<HexCoordinate> path = new LinkedList<>();
        // Walk backwards from destination to origin
        while (cameFrom.containsKey(current)) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }

        steps = new ArrayList<>(path);
    }

    // ---------

    public HexCoordinate getNextStep() {
        return steps.get(0);
    }

    public void nextStep() {
        if (steps.size() > 0) {
            currentPosition = steps.get(0);
            steps.remove(0);
        }
    }

    public boolean isDone() {
        return steps.size() == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Route)) return false;
        return ((Route) obj).routeID.equals(routeID);
    }
}
