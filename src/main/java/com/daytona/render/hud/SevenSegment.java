package com.daytona.render.hud;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Display de 7 segmentos inclinado, como los marcadores de los arcades de los 90.
 * Soporta dígitos, espacio, '-', ':' y '.'.
 */
public final class SevenSegment {

    /** Bits a..g (a = arriba, b = der. arriba, c = der. abajo, d = abajo, e = izq. abajo, f = izq. arriba, g = medio). */
    private static final int[] DIGITS = {
            0b1111110, 0b0110000, 0b1101101, 0b1111001, 0b0110011,
            0b1011011, 0b1011111, 0b1110000, 0b1111111, 0b1111011};
    private static final double SLANT = 0.18;

    private final int[] px = new int[6];
    private final int[] py = new int[6];
    private double baseline;
    private boolean outlineMode;

    /** Ancho total que ocupará el texto. */
    public double width(String text, double digitHeight) {
        double w = 0;
        for (char c : text.toCharArray()) {
            w += advance(c, digitHeight);
        }
        return w;
    }

    private static double advance(char c, double h) {
        return (c == ':' || c == '.') ? h * 0.32 : h * 0.76;
    }

    /**
     * @param x       borde izquierdo
     * @param yBase   línea de base (abajo de los dígitos)
     * @param off     color de los segmentos apagados (null = no se dibujan)
     * @param outline contorno oscuro (null = sin contorno)
     */
    public void draw(Graphics2D g, String text, double x, double yBase, double h, Color on, Color off, Color outline) {
        baseline = yBase;
        double cx = x;
        for (char c : text.toCharArray()) {
            if (c == ':' || c == '.') {
                dots(g, c, cx, h, on, outline);
            } else {
                int mask = mask(c);
                if (off != null) {
                    digit(g, 0b1111111, cx, h, off, null);
                }
                if (mask > 0) {
                    digit(g, mask, cx, h, on, outline);
                }
            }
            cx += advance(c, h);
        }
    }

    private static int mask(char c) {
        if (c >= '0' && c <= '9') {
            return DIGITS[c - '0'];
        }
        return c == '-' ? 0b0000001 : 0;
    }

    private void digit(Graphics2D g, int mask, double x, double h, Color color, Color outline) {
        double t = h * 0.13;
        if (outline != null) {
            g.setColor(outline);
            java.awt.Stroke old = g.getStroke();
            g.setStroke(new java.awt.BasicStroke((float) (t * 0.55), java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            outlineMode = true;
            for (int seg = 0; seg < 7; seg++) {
                if ((mask & (1 << (6 - seg))) != 0) {
                    segment(g, seg, x, h, t);
                }
            }
            outlineMode = false;
            g.setStroke(old);
        }
        g.setColor(color);
        for (int seg = 0; seg < 7; seg++) {
            if ((mask & (1 << (6 - seg))) != 0) {
                segment(g, seg, x, h, t);
            }
        }
    }

    private void segment(Graphics2D g, int seg, double x, double h, double t) {
        double w = h * 0.5;
        double top = baseline - h;
        double mid = baseline - h / 2;
        switch (seg) {
            case 0:
                horizontal(g, x, top, w, t);
                break;
            case 1:
                vertical(g, x + w, top, h / 2, t);
                break;
            case 2:
                vertical(g, x + w, mid, h / 2, t);
                break;
            case 3:
                horizontal(g, x, baseline, w, t);
                break;
            case 4:
                vertical(g, x, mid, h / 2, t);
                break;
            case 5:
                vertical(g, x, top, h / 2, t);
                break;
            default:
                horizontal(g, x, mid, w, t);
                break;
        }
    }

    private void horizontal(Graphics2D g, double x, double y, double w, double t) {
        double ht = t / 2;
        double gap = t * 0.3;
        point(0, x + gap, y);
        point(1, x + gap + ht, y - ht);
        point(2, x + w - gap - ht, y - ht);
        point(3, x + w - gap, y);
        point(4, x + w - gap - ht, y + ht);
        point(5, x + gap + ht, y + ht);
        paint(g);
    }

    private void vertical(Graphics2D g, double x, double y, double len, double t) {
        double ht = t / 2;
        double gap = t * 0.3;
        point(0, x, y + gap);
        point(1, x + ht, y + gap + ht);
        point(2, x + ht, y + len - gap - ht);
        point(3, x, y + len - gap);
        point(4, x - ht, y + len - gap - ht);
        point(5, x - ht, y + gap + ht);
        paint(g);
    }

    private void paint(Graphics2D g) {
        if (outlineMode) {
            g.drawPolygon(px, py, 6);
        }
        g.fillPolygon(px, py, 6);
    }

    /** Guarda un vértice aplicando la inclinación itálica. */
    private void point(int i, double x, double y) {
        px[i] = (int) Math.round(x + (baseline - y) * SLANT);
        py[i] = (int) Math.round(y);
    }

    private void dots(Graphics2D g, char c, double x, double h, Color on, Color outline) {
        double d = Math.max(2, h * 0.13);
        double cx = x + h * 0.06;
        if (outline != null) {
            g.setColor(outline);
            g.fillRect((int) (cx - d * 0.4), (int) (baseline - d * 1.4), (int) (d * 1.8), (int) (d * 1.8));
            if (c == ':') {
                g.fillRect((int) (cx - d * 0.4 + h * 0.07), (int) (baseline - h * 0.62 - d * 0.4), (int) (d * 1.8), (int) (d * 1.8));
            }
        }
        g.setColor(on);
        g.fillRect((int) cx, (int) (baseline - d), (int) d, (int) d);
        if (c == ':') {
            g.fillRect((int) (cx + h * 0.07), (int) (baseline - h * 0.62), (int) d, (int) d);
        }
    }
}
