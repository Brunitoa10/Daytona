package com.daytona.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Random;

/** Humo de gomas, tierra y chispas en coordenadas de pantalla (sin crear objetos por partícula). */
public final class ParticleSystem {

    public enum Kind { SMOKE, DUST, SPARK }

    private static final int MAX = 600;

    private final float[] x = new float[MAX];
    private final float[] y = new float[MAX];
    private final float[] vx = new float[MAX];
    private final float[] vy = new float[MAX];
    private final float[] life = new float[MAX];
    private final float[] maxLife = new float[MAX];
    private final float[] size = new float[MAX];
    private final int[] rgb = new int[MAX];
    private final byte[] kind = new byte[MAX];
    private final Random random = new Random();
    private final Color[] cache = new Color[1];
    private int next;

    public void emit(Kind k, double px, double py, double scale, int color, int count) {
        for (int c = 0; c < count; c++) {
            int i = next;
            next = (next + 1) % MAX;
            x[i] = (float) (px + (random.nextDouble() - 0.5) * 10 * scale);
            y[i] = (float) py;
            switch (k) {
                case SMOKE:
                    vx[i] = (float) ((random.nextDouble() - 0.5) * 60 * scale);
                    vy[i] = (float) (-30 - random.nextDouble() * 40 * scale);
                    maxLife[i] = 0.6f + random.nextFloat() * 0.4f;
                    size[i] = (float) (10 * scale + random.nextDouble() * 8 * scale);
                    break;
                case DUST:
                    vx[i] = (float) ((random.nextDouble() - 0.5) * 120 * scale);
                    vy[i] = (float) (-40 - random.nextDouble() * 60 * scale);
                    maxLife[i] = 0.5f + random.nextFloat() * 0.3f;
                    size[i] = (float) (8 * scale + random.nextDouble() * 8 * scale);
                    break;
                case SPARK:
                    vx[i] = (float) ((random.nextDouble() - 0.5) * 400 * scale);
                    vy[i] = (float) (-80 - random.nextDouble() * 200 * scale);
                    maxLife[i] = 0.25f + random.nextFloat() * 0.25f;
                    size[i] = (float) (2 * scale + 1);
                    break;
            }
            life[i] = maxLife[i];
            rgb[i] = color;
            kind[i] = (byte) k.ordinal();
        }
    }

    public void update(double dt) {
        float d = (float) dt;
        for (int i = 0; i < MAX; i++) {
            if (life[i] <= 0) {
                continue;
            }
            life[i] -= d;
            x[i] += vx[i] * d;
            y[i] += vy[i] * d;
            if (kind[i] == Kind.SPARK.ordinal()) {
                vy[i] += 600 * d;
            } else {
                size[i] += 30 * d;
                vx[i] *= 0.97f;
            }
        }
    }

    public void render(Graphics2D g) {
        Composite old = g.getComposite();
        for (int i = 0; i < MAX; i++) {
            if (life[i] <= 0) {
                continue;
            }
            float t = life[i] / maxLife[i];
            float a = kind[i] == Kind.SPARK.ordinal() ? t : t * 0.45f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(1, a))));
            if (cache[0] == null || cache[0].getRGB() != (0xFF000000 | rgb[i])) {
                cache[0] = new Color(rgb[i]);
            }
            g.setColor(cache[0]);
            int s = (int) size[i];
            g.fillOval((int) x[i] - s / 2, (int) y[i] - s / 2, s, s);
        }
        g.setComposite(old);
    }

    public void clear() {
        java.util.Arrays.fill(life, 0);
    }
}
