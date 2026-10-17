package com.daytona.render.hud;

import com.daytona.config.Settings;
import com.daytona.domain.car.Car;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.Racer;
import com.daytona.domain.track.TrackConstants;
import com.daytona.render.UiKit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

/** Indicadores modernos: tacómetro circular con aguja y tipografía limpia. */
public final class ModernHudSkin implements HudSkin {

    private final Settings settings;
    private java.awt.image.BufferedImage tachoBase;

    public ModernHudSkin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void drawPanels(Graphics2D g, int w, int h, double s, Race race, Racer player, double clock) {
        Car car = player.car();
        // ---- reloj / tiempo ----
        if (race.setup().mode().timeLimited()) {
            double t = race.timeLeft();
            boolean danger = t < 10 && race.phase() == Race.Phase.RACING;
            Color c = danger && (clock % 0.5 < 0.25) ? UiKit.BAD : UiKit.ACCENT;
            UiKit.shadowText(g, "TIEMPO", w / 2.0, 26 * s, UiKit.bodyBold((float) (14 * s)), UiKit.TEXT, UiKit.Align.CENTER);
            UiKit.outlinedText(g, Integer.toString((int) Math.ceil(t)), w / 2.0, 78 * s,
                    UiKit.display((float) (56 * s)), c, UiKit.Align.CENTER);
        } else {
            UiKit.shadowText(g, "TIEMPO", w / 2.0, 26 * s, UiKit.bodyBold((float) (14 * s)), UiKit.TEXT, UiKit.Align.CENTER);
            UiKit.outlinedText(g, UiKit.formatTime(race.elapsed()), w / 2.0, 58 * s,
                    UiKit.display((float) (30 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
        }

        // ---- vuelta ----
        int lap = Math.max(1, Math.min(race.laps(), player.lap() + 1));
        UiKit.shadowText(g, "VUELTA", 20 * s, 26 * s, UiKit.bodyBold((float) (14 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.outlinedText(g, lap + "/" + race.laps(), 20 * s, 64 * s, UiKit.display((float) (36 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.shadowText(g, "ACTUAL  " + UiKit.formatTime(race.currentLapTime()), 20 * s, 86 * s,
                UiKit.bodyBold((float) (13 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.shadowText(g, "MEJOR   " + UiKit.formatTime(player.bestLap()), 20 * s, 104 * s,
                UiKit.bodyBold((float) (13 * s)), UiKit.MUTED, UiKit.Align.LEFT);

        // ---- posición ----
        int total = race.racers().size();
        if (total > 1) {
            UiKit.shadowText(g, "POSICIÓN", w - 20 * s, 26 * s, UiKit.bodyBold((float) (14 * s)), UiKit.TEXT, UiKit.Align.RIGHT);
            UiKit.outlinedText(g, UiKit.ordinal(player.position()), w - 64 * s, 78 * s,
                    UiKit.display((float) (54 * s)), UiKit.ACCENT, UiKit.Align.RIGHT);
            UiKit.shadowText(g, "/" + total, w - 20 * s, 78 * s, UiKit.display((float) (24 * s)), UiKit.TEXT, UiKit.Align.RIGHT);
        }

        drawTacho(g, w, h, s, car);
    }

    private void drawTacho(Graphics2D g, int w, int h, double s, Car car) {
        double r = 70 * s;
        double cx = w - r - 26 * s;
        double cy = h - r - 30 * s;
        int size = (int) Math.ceil(r * 2 + 20 * s);
        if (tachoBase == null || tachoBase.getWidth() != size) {
            tachoBase = buildTachoBase(size, r, s);
        }
        g.drawImage(tachoBase, (int) (cx - size / 2.0), (int) (cy - size / 2.0), null);
        float stroke = (float) (9 * s);
        double start = 225;
        double sweep = -270;
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        double rpm = Math.min(1, car.rpm());
        g.setColor(rpm > 0.86 ? UiKit.BAD : UiKit.ACCENT);
        g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, start, sweep * rpm, Arc2D.OPEN));
        double na = Math.toRadians(start + sweep * rpm);
        g.setStroke(new BasicStroke((float) (3 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(UiKit.ACCENT2);
        g.draw(new Line2D.Double(cx, cy, cx + Math.cos(na) * (r - 10 * s), cy - Math.sin(na) * (r - 10 * s)));
        g.setColor(UiKit.TEXT);
        g.fillOval((int) (cx - 5 * s), (int) (cy - 5 * s), (int) (10 * s), (int) (10 * s));

        double kmh = TrackConstants.unitsToKmh(car.speed());
        String speed = Integer.toString((int) Math.round(settings.speedUnit().convert(kmh)));
        UiKit.outlinedText(g, speed, cx, cy + r * 0.62, UiKit.display((float) (30 * s)), UiKit.TEXT, UiKit.Align.CENTER);
        UiKit.text(g, settings.speedUnit().label(), cx, cy + r * 0.88, UiKit.bodyBold((float) (11 * s)), UiKit.MUTED, UiKit.Align.CENTER);

        String gear = car.gear() == 0 ? "N" : Integer.toString(car.gear());
        double gx = cx - r - 34 * s;
        double gy = cy + r * 0.2;
        UiKit.panel(g, gx - 24 * s, gy - 44 * s, 48 * s, 58 * s);
        UiKit.outlinedText(g, gear, gx, gy + 2 * s, UiKit.display((float) (40 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
        UiKit.text(g, car.transmission().type().shortLabel(), gx, gy + 12 * s, UiKit.bodyBold((float) (10 * s)),
                UiKit.MUTED, UiKit.Align.CENTER);
    }

    /** Parte fija del tacómetro (fondo, escala, zona roja), dibujada una sola vez. */
    private static java.awt.image.BufferedImage buildTachoBase(int size, double r, double s) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        UiKit.smooth(g);
        double cx = size / 2.0;
        double cy = size / 2.0;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillOval((int) (cx - r - 8 * s), (int) (cy - r - 8 * s), (int) (r * 2 + 16 * s), (int) (r * 2 + 16 * s));
        g.setStroke(new BasicStroke((float) (9 * s), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 50));
        g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 225, -270, Arc2D.OPEN));
        g.setColor(UiKit.BAD);
        g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 225 - 270 * 0.86, -270 * 0.14, Arc2D.OPEN));
        g.setStroke(new BasicStroke((float) (1.5 * s)));
        g.setColor(UiKit.TEXT);
        for (int i = 0; i <= 9; i++) {
            double a = Math.toRadians(225 - 270 * i / 9.0);
            g.draw(new Line2D.Double(cx + Math.cos(a) * (r - 14 * s), cy - Math.sin(a) * (r - 14 * s),
                    cx + Math.cos(a) * (r - 7 * s), cy - Math.sin(a) * (r - 7 * s)));
        }
        g.dispose();
        return img;
    }

}
