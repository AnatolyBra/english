package org.example.desktop;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class SuccessOverlay extends JPanel {

    private static final Color[] CORRECT_COLORS = {
            new Color(0x22C55E),
            new Color(0x4ADE80),
            new Color(0x16A34A),
            new Color(0xFACC15),
            new Color(0x38BDF8)
    };
    private static final Color[] CLOSE_COLORS = {
            new Color(0xF59E0B),
            new Color(0xFBBF24),
            new Color(0xFB923C)
    };

    private final List<Particle> particles = new ArrayList<>();
    private final Timer timer;
    private float checkScale;
    private float checkAlpha;
    private int originX;
    private int originY;
    private boolean playCheck;

    SuccessOverlay() {
        setOpaque(false);
        setVisible(false);
        timer = new Timer(16, event -> tick());
    }

    void play(int x, int y, boolean exact) {
        originX = x;
        originY = y;
        playCheck = exact;
        checkScale = 0.2f;
        checkAlpha = 1f;
        particles.clear();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Color[] palette = exact ? CORRECT_COLORS : CLOSE_COLORS;
        int count = exact ? 36 : 22;
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 3.5 + random.nextDouble() * 7.5;
            particles.add(new Particle(
                    x,
                    y,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed - 4,
                    5 + random.nextDouble() * 6,
                    palette[random.nextInt(palette.length)],
                    random.nextInt(3),
                    28 + random.nextInt(18)
            ));
        }
        setVisible(true);
        if (!timer.isRunning()) {
            timer.start();
        }
        repaint();
    }

    void stop() {
        timer.stop();
        particles.clear();
        checkAlpha = 0;
        setVisible(false);
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    private void tick() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().step()) {
                iterator.remove();
            }
        }
        if (playCheck) {
            checkScale = Math.min(1.15f, checkScale + 0.12f);
            if (checkScale > 1f) {
                checkAlpha = Math.max(0f, checkAlpha - 0.06f);
            }
        } else {
            checkAlpha = 0;
        }
        if (particles.isEmpty() && checkAlpha <= 0) {
            stop();
            return;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (playCheck && checkAlpha > 0) {
            paintCheck(g);
        }
        for (Particle particle : particles) {
            particle.paint(g);
        }
        g.dispose();
    }

    private void paintCheck(Graphics2D g) {
        g.setComposite(AlphaComposite.SrcOver.derive(checkAlpha));
        g.setColor(new Color(0x16A34A));
        g.setStroke(new BasicStroke(5.5f * checkScale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double s = 18 * checkScale;
        Path2D tick = new Path2D.Double();
        tick.moveTo(originX - s, originY);
        tick.lineTo(originX - s * 0.25, originY + s * 0.7);
        tick.lineTo(originX + s, originY - s * 0.7);
        g.draw(tick);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private static final class Particle {
        private double x;
        private double y;
        private double vx;
        private double vy;
        private final double size;
        private final Color color;
        private final int shape;
        private int life;
        private final int maxLife;

        private Particle(double x, double y, double vx, double vy, double size, Color color, int shape, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.shape = shape;
            this.life = life;
            this.maxLife = life;
        }

        private boolean step() {
            x += vx;
            y += vy;
            vy += 0.28;
            vx *= 0.985;
            life--;
            return life > 0;
        }

        private void paint(Graphics2D g) {
            float alpha = Math.max(0f, (float) life / maxLife);
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.setColor(color);
            int px = (int) Math.round(x);
            int py = (int) Math.round(y);
            int s = (int) Math.round(size);
            switch (shape) {
                case 1 -> g.fillRoundRect(px, py, s, s, 3, 3);
                case 2 -> {
                    g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(px, py, px + s, py + s / 3);
                    g.drawLine(px + s, py + s / 3, px + s + 4, py - s);
                }
                default -> g.fillOval(px, py, s, s);
            }
            g.setComposite(AlphaComposite.SrcOver);
        }
    }
}
