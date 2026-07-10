package view.game;

import model.game.unit.Unit;

import javax.swing.*;
import java.awt.*;

final class UnitRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        Component component =
                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

        if (value instanceof Unit unit) {
            setText(
                    ViewTextFormatter.pretty(unit.getType())
                            + "  AP "
                            + unit.getCurrentAP()
                            + "/"
                            + unit.getType().getEachTurnAP()
            );
        }

        return component;
    }
}