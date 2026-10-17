package com.daytona.render.hud;

import com.daytona.config.Settings;
import com.daytona.domain.car.Car;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.Racer;
import com.daytona.domain.track.TrackConstants;
import com.daytona.render.UiKit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Indicadores al estilo de los arcades de carreras de 1994: dígitos de 7 segmentos
 * amarillos con contorno, barra de RPM de LEDs, velocidad en rojo y tiempos de vuelta
 * en columna.
 */
public final class Arcade94HudSkin implements HudSkin {

    private static final Color YELLOW = new Color(0xFFD21F);
    private static final Color ORANGE = new Color(0xFF8A00);
    private static final Color RED = new Color(0xFF2A1A);
    private static final Color OUTLINE = new Color(0x220800);
    private static final Color GHOST = new Color(90, 20, 10, 120);
    private static final Color LABEL_SHADOW = new Color(0xB01000);
    private static final Color WHITE = new Color(0xF8F8F8);

    private final Settings settings;
    private final SevenSegment seg = new SevenSegment();

    public Arcade94HudSkin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void drawPanels(Graphics2D g, int w, int h, double s, Race race, Racer player, double clock) {
        drawTime(g, w, s, race, clock);
        drawLaps(g, s, race, player);
        drawPosition(g, w, s, race, player);
        drawSpeedCluster(g, w, h, s, player.car());
    }

    // ------------------------------------------------------------------ bloques

    private void drawTime(Graphics2D g, int w, double s, Race race, double clock) {
        label(g, "TIME", w / 2.0, 22 * s, 17 * s, UiKit.Align.CENTER);
        if (race.setup().mode().timeLimited()) {
            double t = Math.max(0, race.timeLeft());
            String text = Integer.toString((int) Math.ceil(t));
            boolean danger = t < 10 && race.phase() == Race.Phase.RACING;
            Color c = danger && clock % 0.5 < 0.25 ? RED : YELLOW;
            double dh = 52 * s;
            seg.draw(g, text, w / 2.0 - seg.width(text, dh) / 2, 82 * s, dh, c, null, OUTLINE);
        } else {
            String text = clockText(race.elapsed());
            double dh = 30 * s;
            seg.draw(g, text, w / 2.0 - seg.width(text, dh) / 2, 62 * s, dh, YELLOW, null, OUTLINE);
        }
    }

    private void drawLaps(Graphics2D g, double s, Race race, Racer player) {
        int lap = Math.max(1, Math.min(race.laps(), player.lap() + 1));
        label(g, "LAP", 18 * s, 22 * s, 17 * s, UiKit.Align.LEFT);
        seg.draw(g, Integer.toString(lap), 20 * s, 66 * s, 38 * s, YELLOW, null, OUTLINE);
        UiKit.outlinedText(g, "/" + race.laps(), 52 * s, 66 * s, UiKit.display((float) (20 * s)), WHITE, UiKit.Align.LEFT);

        Font mono = new Font(Font.MONOSPACED, Font.BOLD | Font.ITALIC, Math.max(8, (int) (13 * s)));
        double y = 88 * s;
        List<Double> laps = player.lapTimes();
        int from = Math.max(0, laps.size() - 4);
        double best = player.bestLap();
        for (int i = from; i < laps.size(); i++) {
            Color c = laps.get(i) == best ? YELLOW : WHITE;
            UiKit.shadowText(g, String.format("%d  %s", i + 1, arcadeTime(laps.get(i))), 18 * s, y, mono, c, UiKit.Align.LEFT);
            y += 16 * s;
        }
        UiKit.shadowText(g, String.format("%d  %s", laps.size() + 1, arcadeTime(race.currentLapTime())), 18 * s, y, mono,
                ORANGE, UiKit.Align.LEFT);
    }

    private void drawPosition(Graphics2D g, int w, double s, Race race, Racer player) {
        int total = race.racers().size();
        if (total <= 1) {
            return;
        }
        label(g, "POSITION", w - 18 * s, 22 * s, 17 * s, UiKit.Align.RIGHT);
        String pos = Integer.toString(player.position());
        double dh = 50 * s;
        double right = w - 64 * s;
        seg.draw(g, pos, right - seg.width(pos, dh), 80 * s, dh, YELLOW, null, OUTLINE);
        UiKit.outlinedText(g, "º", right + 2 * s, 52 * s, UiKit.display((float) (20 * s)), YELLOW, UiKit.Align.LEFT);
        UiKit.outlinedText(g, "/" + total, w - 18 * s, 80 * s, UiKit.display((float) (20 * s)), WHITE, UiKit.Align.RIGHT);
    }

    private void drawSpeedCluster(Graphics2D g, int w, int h, double s, Car car) {
        double right = w - 20 * s;
        double base = h - 22 * s;
        // fondo inclinado
        g.setColor(new Color(0, 0, 0, 140));
        int[] xs = {(int) (right - 230 * s), (int) (right + 8 * s), (int) (right + 8 * s), (int) (right - 210 * s)};
        int[] ys = {(int) (base - 108 * s), (int) (base - 108 * s), (int) (base + 12 * s), (int) (base + 12 * s)};
        g.fillPolygon(xs, ys, 4);

        // barra de RPM de LEDs
        int leds = 18;
        double ledW = 10 * s;
        double ledGap = 2.4 * s;
        double ledX = right - leds * (ledW + ledGap);
        double ledY = base - 96 * s;
        int lit = (int) Math.round(Math.min(1.05, car.rpm()) * leds);
        for (int i = 0; i < leds; i++) {
            double t = i / (double) leds;
            Color on = t < 0.6 ? new Color(0x3DFF5A) : t < 0.85 ? YELLOW : RED;
            g.setColor(i < lit ? on : new Color(40, 40, 40, 200));
            double x = ledX + i * (ledW + ledGap);
            double hh = (10 + 12 * t) * s;
            int[] lx = {(int) (x + 3 * s), (int) (x + ledW + 3 * s), (int) (x + ledW), (int) x};
            int[] ly = {(int) (ledY - hh), (int) (ledY - hh), (int) ledY, (int) ledY};
            g.fillPolygon(lx, ly, 4);
        }

        // velocidad
        double kmh = TrackConstants.unitsToKmh(car.speed());
        String speed = String.format("%3d", (int) Math.round(settings.speedUnit().convert(kmh)));
        double dh = 46 * s;
        double sw = seg.width("888", dh);
        seg.draw(g, speed, right - sw - 44 * s, base - 8 * s, dh, RED, GHOST, OUTLINE);
        UiKit.shadowText(g, settings.speedUnit().label(), right, base - 8 * s, UiKit.display((float) (15 * s)), WHITE,
                UiKit.Align.RIGHT);

        // marcha
        double gx = right - sw - 112 * s;
        double gy = base - 8 * s;
        label(g, car.transmission().type().shortLabel(), gx + 18 * s, gy - 54 * s, 13 * s, UiKit.Align.CENTER);
        if (car.gear() == 0) {
            UiKit.outlinedText(g, "N", gx + 4 * s, gy, UiKit.display((float) (44 * s)), YELLOW, UiKit.Align.LEFT);
        } else {
            seg.draw(g, Integer.toString(car.gear()), gx + 2 * s, gy, 44 * s, YELLOW, GHOST, OUTLINE);
        }
    }

    // ------------------------------------------------------------------ ayudas

    /** Rótulo itálico amarillo con sombra roja, típico de los arcades. */
    private static void label(Graphics2D g, String text, double x, double y, double size, UiKit.Align align) {
        Font f = UiKit.display((float) size);
        double off = Math.max(1, size / 9);
        UiKit.text(g, text, x + off, y + off, f, LABEL_SHADOW, align);
        UiKit.text(g, text, x, y, f, YELLOW, align);
    }

    /** 1'23"45 */
    static String arcadeTime(double seconds) {
        if (seconds < 0) {
            return "-'--\"--";
        }
        long cs = Math.round(seconds * 100);
        return String.format("%d'%02d\"%02d", cs / 6000, (cs / 100) % 60, cs % 100);
    }

    private static String clockText(double seconds) {
        long cs = Math.round(seconds * 100);
        return String.format("%d:%02d.%02d", cs / 6000, (cs / 100) % 60, cs % 100);
    }
}
