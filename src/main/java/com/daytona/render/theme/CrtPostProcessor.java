package com.daytona.render.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Monitor de arcade: píxeles grandes (vecino más cercano), líneas de barrido y
 * viñeta en las esquinas. La máscara se genera una vez por tamaño de ventana.
 */
public final class CrtPostProcessor implements PostProcessor {

    private BufferedImage mask;
    private int maskW;
    private int maskH;
    private int maskRows;

    @Override
    public Object interpolation() {
        return RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    }

    @Override
    public void overlay(Graphics2D screen, int x, int y, int w, int h, int sourceHeight) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (mask == null || maskW != w || maskH != h || maskRows != sourceHeight) {
            mask = buildMask(w, h, sourceHeight);
            maskW = w;
            maskH = h;
            maskRows = sourceHeight;
        }
        screen.drawImage(mask, x, y, null);
    }

    private static BufferedImage buildMask(int w, int h, int rows) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        double pixel = h / (double) Math.max(1, rows);
        g.setColor(new Color(0, 0, 0, pixel >= 2.5 ? 70 : 40));
        if (pixel >= 2) {
            int thickness = Math.max(1, (int) Math.round(pixel / 3));
            for (int r = 0; r < rows; r++) {
                int yy = (int) Math.round((r + 1) * pixel) - thickness;
                g.fillRect(0, yy, w, thickness);
            }
        } else {
            for (int yy = 1; yy < h; yy += 2) {
                g.fillRect(0, yy, w, 1);
            }
        }
        float radius = (float) Math.hypot(w, h) / 2f;
        g.setPaint(new RadialGradientPaint(w / 2f, h / 2f, radius, new float[]{0.6f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)}));
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }
}
