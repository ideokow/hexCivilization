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
            String owner = unit.isOwnedByPlayer() ? "Player " : "Tribe ";
            setText(
                    owner
                            + ViewTextFormatter.pretty(unit.getType())
                            + "  AP "
                            + unit.getCurrentAP()
                            + "/"
                            + unit.getType().getEachTurnAP()
            );
        }

        setForeground(new Color(234, 238, 245));
        setBackground(
                isSelected
                        ? new Color(62, 83, 108)
                        : new Color(35, 45, 61)
        );
        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        return component;
    }
}
