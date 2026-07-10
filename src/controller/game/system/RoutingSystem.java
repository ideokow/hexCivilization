package controller.game.system;

import controller.game.GameController;
import model.game.hex.HexCoordinate;
import model.game.hex.HexGrid;
import model.game.route.Route;
import model.game.unit.Unit;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingSystem {

    private final MovementSystem movementSystem;

    public RoutingSystem(MovementSystem movementSystem) {
        this.movementSystem = movementSystem;
    }

    // game controller needed for alerts
    public Route route(HexCoordinate origin, HexCoordinate destination, HexGrid hexGrid, GameController gameController) {
        try {
            return new Route(origin, destination, hexGrid);
        } catch (IllegalArgumentException e) {
            gameController.toastAlert(e.getMessage());
            return null;
        }
    }

    /*
    a function for update routes in each turn
    game controller needed for alert trigger
     */
    public void moveUnits(Map<Route, Unit> inQueueRoutes, GameController gameController) {
        // store finished ones (blocked or arrived)
        List<Route> finishedRoutes = new ArrayList<>();
        try {
            for (Route thisRoute : inQueueRoutes.keySet()) {
                Unit thisUnit = inQueueRoutes.get(thisRoute);
                handleRoute(thisRoute, thisUnit, finishedRoutes, gameController);
            }
        } catch (IllegalStateException exception) {
            gameController.toastAlert(exception.getMessage());
        } finally {
            for (Route finishedRoute : finishedRoutes) {
                inQueueRoutes.remove(finishedRoute);
            }
        }
    }

    // game controller needed for alert trigger
    private void handleRoute(Route thisRoute, Unit thisUnit, List<Route> finishedRoutes, GameController gameController) {
        boolean canContinue = true;
        while (canContinue) {
            MoveResult moveResult = movementSystem.move(thisUnit, thisRoute.getNextStep());

            if (moveResult == MoveResult.SUCCESS) {
                thisRoute.nextStep();
                // TODO: #UI movement animation

                if (thisRoute.isDone()) {
                    gameController.toastAlert(thisUnit.getType().getName() + " unit has arrived!");
                    finishedRoutes.add(thisRoute);
                    break;
                }
            } else {
                canContinue = false;

                if (
                    moveResult == MoveResult.UNIT_NOT_ON_MAP ||
                    moveResult == MoveResult.HEX_NOT_DISCOVERED ||
                    moveResult == MoveResult.NOT_NEIGHBOR ||
                    moveResult == MoveResult.UNIT_IS_IN_BUILDING
                ) {
                    finishedRoutes.add(thisRoute);
                    throw new IllegalStateException(thisUnit.getType().getName() + "'s route was wrong!");
                }
            }
        }
    }
}
