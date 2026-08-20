package view.game.combat;

import javax.swing.*;
import java.awt.*;

/** Draws a single die face (1..6) with pips. */
public class DieView extends JComponent {

    private int value;
    private Color highlight;

    public DieView() {
        setPreferredSize(new Dimension(56, 56));
    }

    public void setValue(int value) {
        this.value = value;
        repaint();
    }

    public void setHighlight(Color color) {
        highlight = color;
        repaint();
    }

    public void clearHighlight() {
        highlight = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int size = Math.min(getWidth(), getHeight()) - 8;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        graphics2D.setColor(highlight == null ? Color.WHITE : highlight);
        graphics2D.fillRoundRect(x, y, size, size, 14, 14);
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.drawRoundRect(x, y, size, size, 14, 14);

        if (value >= 1 && value <= 6) {
            drawPips(graphics2D, x, y, size, value);
        }
        graphics2D.dispose();
    }

    private void drawPips(
            Graphics2D graphics2D,
            int x,
            int y,
            int size,
            int value
    ) {
        graphics2D.setColor(Color.BLACK);
        int diameter = size / 5;
        int left = x + size / 4 - diameter / 2;
        int center = x + size / 2 - diameter / 2;
        int right = x + size * 3 / 4 - diameter / 2;
        int top = y + size / 4 - diameter / 2;
        int middle = y + size / 2 - diameter / 2;
        int bottom = y + size * 3 / 4 - diameter / 2;

        boolean topLeft = value == 4 || value == 5 || value == 6;
        boolean topRight = value >= 2;
        boolean middleLeft = value == 6;
        boolean middleRight = value == 6;
        boolean centerPip = value % 2 == 1;
        boolean bottomLeft = value >= 2;
        boolean bottomRight = value == 4 || value == 5 || value == 6;

        if (topLeft) graphics2D.fillOval(left, top, diameter, diameter);
        if (topRight) graphics2D.fillOval(right, top, diameter, diameter);
        if (middleLeft) graphics2D.fillOval(left, middle, diameter, diameter);
        if (centerPip) graphics2D.fillOval(center, middle, diameter, diameter);
        if (middleRight) graphics2D.fillOval(right, middle, diameter, diameter);
        if (bottomLeft) graphics2D.fillOval(left, bottom, diameter, diameter);
        if (bottomRight) graphics2D.fillOval(right, bottom, diameter, diameter);
    }
}
