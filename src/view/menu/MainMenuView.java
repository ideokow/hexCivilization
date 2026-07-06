package view.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/**
 * main menu view template
 * no logic
 */
public class MainMenuView extends JFrame {

    private JButton startButton;
    private JButton settingsButton;
    private JButton exitButton;

    public MainMenuView() {
        setTitle("Strategy Game - Main Menu");
        setSize(700, 560);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        add(buildMenuPanel());
    }

    private JPanel buildMenuPanel() {
        JPanel panel = new BackgroundPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Title with shadow effect ---
        JLabel titleLabel = new ShadowLabel("STRATEGY GAME");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 44));
        titleLabel.setForeground(new Color(235, 240, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(titleLabel, gbc);

        // --- Subtitle ---
        JLabel subtitleLabel = new JLabel("— Conquer  •  Build  •  Rule —");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(150, 165, 210));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(subtitleLabel, gbc);

        // --- Buttons ---
        startButton = new MenuButton("START", new Color(88, 130, 220));
        settingsButton = new MenuButton("SETTINGS", new Color(90, 105, 150));
        exitButton = new MenuButton("EXIT", new Color(160, 70, 85));

        gbc.insets = new Insets(9, 0, 9, 0);
        gbc.gridy = 2; panel.add(startButton, gbc);
        gbc.gridy = 3; panel.add(settingsButton, gbc);
        gbc.gridy = 4; panel.add(exitButton, gbc);

        // --- Footer ---
        JLabel footerLabel = new JLabel("v1.0");
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(90, 100, 135));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 5;
        gbc.insets = new Insets(40, 0, 0, 0);
        panel.add(footerLabel, gbc);

        return panel;
    }

    // ================= Custom Components =================

    private static class BackgroundPanel extends JPanel {
        private final int[][] stars; // x, y, size, alpha

        BackgroundPanel() {
            // Generate fixed random star positions once
            Random rnd = new Random();
            stars = new int[70][4];
            for (int[] s : stars) {
                s[0] = rnd.nextInt(700);
                s[1] = rnd.nextInt(560);
                s[2] = 1 + rnd.nextInt(2);
                s[3] = 60 + rnd.nextInt(140);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Main vertical gradient
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(22, 30, 58),
                    0, getHeight(), new Color(8, 10, 22)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Subtle glow at the top center
            g2.setPaint(new RadialGradientPaint(
                    new Point(getWidth() / 2, 60), 320,
                    new float[]{0f, 1f},
                    new Color[]{new Color(80, 110, 200, 55), new Color(0, 0, 0, 0)}));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Decorative stars
            for (int[] s : stars) {
                g2.setColor(new Color(255, 255, 255, s[3]));
                g2.fillOval(s[0], s[1], s[2], s[2]);
            }
            g2.dispose();
        }
    }

    private static class ShadowLabel extends JLabel {
        ShadowLabel(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            // Shadow layer
            g2.setFont(getFont());
            g2.setColor(new Color(0, 0, 0, 130));
            g2.drawString(getText(), x + 3, y + 3);

            // Main text
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class MenuButton extends JButton {
        private final Color baseColor;
        private float hoverProgress = 0f; // 0 = normal, 1 = fully hovered
        private final Timer animTimer;
        private boolean hovering = false;

        MenuButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;

            setPreferredSize(new Dimension(260, 52));
            setFont(new Font("SansSerif", Font.BOLD, 17));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Smooth hover animation (~60 FPS)
            animTimer = new Timer(16, e -> {
                float target = hovering ? 1f : 0f;
                hoverProgress += (target - hoverProgress) * 0.25f;
                if (Math.abs(target - hoverProgress) < 0.01f) {
                    hoverProgress = target;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    animTimer.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    animTimer.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 26;
            Shape shape = new RoundRectangle2D.Float(
                    1, 1, getWidth() - 2, getHeight() - 2, arc, arc);

            // Interpolate colors based on hover progress
            Color top = brighten(baseColor, 0.15f + 0.35f * hoverProgress);
            Color bottom = brighten(baseColor, -0.15f + 0.20f * hoverProgress);
            if (getModel().isPressed()) {
                top = brighten(baseColor, -0.10f);
                bottom = brighten(baseColor, -0.25f);
            }

            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fill(shape);

            // Border with slight glow on hover
            g2.setColor(new Color(255, 255, 255,
                    (int) (50 + 110 * hoverProgress)));
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(shape);

            g2.dispose();
            super.paintComponent(g);
        }

        private static Color brighten(Color c, float factor) {
            int r = clamp(c.getRed() + (int) (255 * factor));
            int g = clamp(c.getGreen() + (int) (255 * factor));
            int b = clamp(c.getBlue() + (int) (255 * factor));
            return new Color(r, g, b);
        }

        private static int clamp(int v) {
            return Math.max(0, Math.min(255, v));
        }
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }
}
