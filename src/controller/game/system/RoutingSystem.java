package controller.game.system;

import model.game.route.Route;
import model.game.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoutingSystem {

    private final MovementSystem movementSystem;

    public RoutingSystem(MovementSystem movementSystem) {
        this.movementSystem = movementSystem;
    }

    public void moveUnits(Map<Route, Unit> inQueueRoutes) {
        // store finished ones (blocked or arrived)
        List<Route> finishedRoutes = new ArrayList<>();
        try {
            for (Route thisRoute : inQueueRoutes.keySet()) {
                Unit thisUnit = inQueueRoutes.get(thisRoute);
                handleRoute(thisRoute, thisUnit, finishedRoutes);
            }
        } catch (IllegalStateException exception) {
            // TODO: #UI show that route failed
        } finally {
            for (Route finishedRoute : finishedRoutes) {
                inQueueRoutes.remove(finishedRoute);
            }
        }
    }

    private void handleRoute(Route thisRoute, Unit thisUnit, List<Route> finishedRoutes) {
        boolean canContinue = true;
        while (canContinue) {
            MoveResult moveResult = movementSystem.move(thisUnit, thisRoute.getNextStep());

            if (moveResult == MoveResult.SUCCESS) {
                thisRoute.nextStep();
                // TODO: #UI movement animation

                if (thisRoute.isDone()) {
                    // TODO: #UI successfully finished animation
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
                    throw new IllegalStateException("Route was wrong!");
                }
            }
        }
    }
}
