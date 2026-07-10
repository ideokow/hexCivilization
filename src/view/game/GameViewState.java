package view.game;

import model.game.hex.HexCoordinate;
import model.game.unit.Unit;

import java.util.function.Consumer;

final class GameViewState {

    private HexCoordinate selectedHex;
    private Unit selectedUnit;
    private Consumer<HexCoordinate> hexClickHandler;

    HexCoordinate getSelectedHex() {
        return selectedHex;
    }

    void setSelectedHex(HexCoordinate selectedHex) {
        this.selectedHex = selectedHex;
    }

    Unit getSelectedUnit() {
        return selectedUnit;
    }

    void setSelectedUnit(Unit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    void setHexClickHandler(
            Consumer<HexCoordinate> hexClickHandler
    ) {
        this.hexClickHandler = hexClickHandler;
    }

    void notifyHexClicked(HexCoordinate coordinate) {
        if (hexClickHandler != null) {
            hexClickHandler.accept(coordinate);
        }
    }
}