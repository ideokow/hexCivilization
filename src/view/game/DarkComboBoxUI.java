package view.game;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public final class DarkComboBoxUI extends BasicComboBoxUI {

    private static final Color TEXT_PRIMARY = new Color(234, 238, 245);
    private static final Color CONTROL_BACKGROUND = new Color(35, 45, 61);
    private static final Color BORDER_COLOR = new Color(67, 83, 105);
    private static final Color ARROW_BACKGROUND = new Color(42, 57, 76);

    @Override
    protected JButton createArrowButton() {
        BasicArrowButton button = new BasicArrowButton(
                SwingConstants.SOUTH,
                ARROW_BACKGROUND,
                ARROW_BACKGROUND,
                TEXT_PRIMARY,
                ARROW_BACKGROUND
        );

        button.setOpaque(true);
        button.setFocusable(false);
        button.setBorder(
                BorderFactory.createMatteBorder(
                        0,
                        1,
                        0,
                        0,
                        BORDER_COLOR
                )
        );

        return button;
    }

    @Override
    public void paintCurrentValueBackground(
            Graphics graphics,
            Rectangle bounds,
            boolean hasFocus
    ) {
        graphics.setColor(
                comboBox.isEnabled()
                        ? CONTROL_BACKGROUND
                        : new Color(45, 49, 57)
        );
        graphics.fillRect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
        );
    }
}
