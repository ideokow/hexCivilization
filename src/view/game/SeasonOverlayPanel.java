package view.game;

import model.game.season.SeasonName;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

/**
 * Paints the current season over the map without taking part in mouse input.
 * The season itself is supplied by the game view model, which delegates to
 * {@code GameEngine.getSeason()}.
 */
final class SeasonOverlayPanel extends JPanel {

    private static final int MAX_PARTICLES = 24;
    private static final int FRAME_DELAY_MS = 50;

    private static final Color WINTER_TINT = new Color(150, 195, 255, 28);
    private static final Color FALL_TINT = new Color(90, 105, 130, 34);
    private static final Color SPRING_TINT = new Color(140, 220, 150, 16);
    private static final Color SUMMER_TINT = new Color(255, 200, 90, 22);

    private final Particle[] particles = new Particle[MAX_PARTICLES];
    private final Random random = new Random();
    private final Timer animationTimer;

    private SeasonName season = SeasonName.SPRING;
    private int activeParticleCount;
    private double clock;

    SeasonOverlayPanel() {
        setOpaque(false);
        setFocusable(false);
        setLayout(null);

        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle();
        }
        seedParticles();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                seedParticles();
            }
        });

        animationTimer = new Timer(FRAME_DELAY_MS, event -> {
            clock += FRAME_DELAY_MS / 1000.0;
            updateParticles();
            repaint();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        animationTimer.start();
    }

    @Override
    public void removeNotify() {
        animationTimer.stop();
        super.removeNotify();
    }

    void setSeason(SeasonName season) {
        if (season == null || season == this.season) {
            return;
        }

        this.season = season;
        seedParticles();
        repaint();
    }

    /**
     * Lets the overlay remain transparent to map clicks and mouse gestures.
     */
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    private static final class Particle {
        double x;
        double y;
        double vx;
        double vy;
        double size;
        double phase;
        double spin;
        float alpha;
    }

    private int particleCountFor(SeasonName season) {
        return switch (season) {
            case WINTER -> 20;
            case FALL -> 24;
            case SPRING -> 12;
            case SUMMER -> 8;
        };
    }

    private void seedParticles() {
        activeParticleCount = Math.min(
                particleCountFor(season),
                particles.length
        );

        for (int i = 0; i < activeParticleCount; i++) {
            resetParticle(particles[i], true);
        }
    }

    private void resetParticle(Particle particle, boolean randomY) {
        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);

        particle.x = random.nextDouble() * width;
        particle.y = randomY ? random.nextDouble() * height : -20;
        particle.phase = random.nextDouble() * Math.PI * 2;
        particle.spin = (random.nextDouble() - 0.5) * 0.08;

        switch (season) {
            case WINTER -> {
                particle.size = 1.5 + random.nextDouble() * 3.5;
                particle.vy = 0.6 + random.nextDouble() * 1.5;
                particle.vx = -0.3 + random.nextDouble() * 0.6;
                particle.alpha = 0.55f + random.nextFloat() * 0.45f;
            }
            case FALL -> {
                particle.size = 7 + random.nextDouble() * 9;
                particle.vy = 7 + random.nextDouble() * 6;
                particle.vx = 2.2 + random.nextDouble() * 2.2;
                particle.alpha = 0.35f + random.nextFloat() * 0.35f;
            }
            case SPRING -> {
                particle.size = 4 + random.nextDouble() * 5;
                particle.vy = 0.8 + random.nextDouble();
                particle.vx = 0.4 + random.nextDouble() * 0.9;
                particle.alpha = 0.6f + random.nextFloat() * 0.4f;
            }
            case SUMMER -> {
                particle.size = 1.5 + random.nextDouble() * 2.5;
                particle.vy = -0.25 - random.nextDouble() * 0.35;
                particle.vx = -0.2 + random.nextDouble() * 0.4;
                particle.alpha = 0.25f + random.nextFloat() * 0.35f;
            }
        }
    }

    private void updateParticles() {
        if (activeParticleCount == 0) {
            return;
        }

        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);
        double gust = Math.sin(clock * 0.7) * 1.2;

        for (int i = 0; i < activeParticleCount; i++) {
            Particle particle = particles[i];
            particle.phase += particle.spin != 0 ? particle.spin : 0.03;

            switch (season) {
                case WINTER -> {
                    particle.x += particle.vx + Math.sin(particle.phase) * 0.6;
                    particle.y += particle.vy;
                }
                case FALL -> {
                    particle.x += particle.vx + gust;
                    particle.y += particle.vy;
                }
                case SPRING -> {
                    particle.x += particle.vx
                            + Math.sin(particle.phase * 1.4) * 0.9
                            + gust * 0.4;
                    particle.y += particle.vy + Math.cos(particle.phase) * 0.15;
                }
                case SUMMER -> {
                    particle.x += particle.vx + Math.sin(particle.phase) * 0.3;
                    particle.y += particle.vy;
                }
            }

            boolean outsideVertically = season == SeasonName.SUMMER
                    ? particle.y < -20
                    : particle.y > height + 20;
            if (outsideVertically
                    || particle.x > width + 30
                    || particle.x < -30) {
                resetParticle(particle, false);
                if (season == SeasonName.SUMMER) {
                    particle.y = height + 10;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        try {
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics2D.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE
            );

            drawTint(graphics2D);
            drawParticles(graphics2D);
        } finally {
            graphics2D.dispose();
        }
    }

    private void drawTint(Graphics2D graphics2D) {
        Color tint = switch (season) {
            case WINTER -> WINTER_TINT;
            case FALL -> FALL_TINT;
            case SPRING -> SPRING_TINT;
            case SUMMER -> SUMMER_TINT;
        };

        graphics2D.setColor(tint);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawParticles(Graphics2D graphics2D) {
        switch (season) {
            case WINTER -> drawSnow(graphics2D);
            case FALL -> drawRain(graphics2D);
            case SPRING -> drawPetals(graphics2D);
            case SUMMER -> drawDust(graphics2D);
        }
    }

    private void drawSnow(Graphics2D graphics2D) {
        for (int i = 0; i < activeParticleCount; i++) {
            Particle particle = particles[i];
            graphics2D.setColor(new Color(1f, 1f, 1f, particle.alpha));
            graphics2D.fill(new Ellipse2D.Double(
                    particle.x,
                    particle.y,
                    particle.size,
                    particle.size
            ));
        }
    }

    private void drawRain(Graphics2D graphics2D) {
        graphics2D.setStroke(new BasicStroke(
                1.4f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        for (int i = 0; i < activeParticleCount; i++) {
            Particle particle = particles[i];
            graphics2D.setColor(new Color(
                    0.72f,
                    0.82f,
                    0.95f,
                    particle.alpha
            ));
            double endX = particle.x - particle.vx * 0.9;
            double endY = particle.y - particle.size;
            graphics2D.drawLine(
                    (int) particle.x,
                    (int) particle.y,
                    (int) endX,
                    (int) endY
            );
        }
    }

    private void drawPetals(Graphics2D graphics2D) {
        for (int i = 0; i < activeParticleCount; i++) {
            Particle particle = particles[i];
            double widthScale = Math.abs(Math.cos(particle.phase)) * 0.75 + 0.25;
            double petalWidth = particle.size * widthScale;
            double petalHeight = particle.size * 0.7;

            graphics2D.setColor(new Color(
                    1f,
                    0.72f,
                    0.82f,
                    particle.alpha
            ));
            graphics2D.fill(new Ellipse2D.Double(
                    particle.x,
                    particle.y,
                    petalWidth,
                    petalHeight
            ));
        }
    }

    private void drawDust(Graphics2D graphics2D) {
        for (int i = 0; i < activeParticleCount; i++) {
            Particle particle = particles[i];
            float alpha = (float) (
                    particle.alpha
                            * (0.6 + 0.4 * Math.sin(particle.phase * 2))
            );

            graphics2D.setColor(new Color(
                    1f,
                    0.94f,
                    0.7f,
                    Math.max(0f, Math.min(1f, alpha))
            ));
            graphics2D.fill(new Ellipse2D.Double(
                    particle.x,
                    particle.y,
                    particle.size,
                    particle.size
            ));
        }
    }

}
