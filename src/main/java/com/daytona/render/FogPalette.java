package com.daytona.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Caché de colores mezclados con la niebla (Flyweight): evita crear miles de
 * objetos {@link Color} por frame.
 */
public final class FogPalette {

    public static final int LEVELS = 32;

    private final int fogRgb;
    private final Map<Integer, Color[]> cache = new HashMap<>();

    public FogPalette(int fogRgb) {
        this.fogRgb = fogRgb;
    }

    /** @param level 0 = sin niebla, LEVELS-1 = todo niebla */
    public Color get(int rgb, int level) {
        Color[] ramp = cache.computeIfAbsent(rgb, this::buildRamp);
        return ramp[Math.max(0, Math.min(LEVELS - 1, level))];
    }

    private Color[] buildRamp(int rgb) {
        Color[] ramp = new Color[LEVELS];
        for (int i = 0; i < LEVELS; i++) {
            ramp[i] = new Color(blend(rgb, fogRgb, i / (double) (LEVELS - 1)));
        }
        return ramp;
    }

    public static int blend(int a, int b, double t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) Math.round(ar + (br - ar) * t);
        int g = (int) Math.round(ag + (bg - ag) * t);
        int bl = (int) Math.round(ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }

    public static int darker(int rgb, double amount) {
        return blend(rgb, 0x000000, amount);
    }
}
