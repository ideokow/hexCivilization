package model.game.route;

import model.game.hex.HexCoordinate;

public final class Node {
    final HexCoordinate coordinate;
    final int fScore;

    Node(HexCoordinate coordinate, int fScore) {
        this.coordinate = coordinate;
        this.fScore = fScore;
    }
}
