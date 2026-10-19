package com.daytona.render.art;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Dibuja autos de carrera vistos desde atrás, por código (sin archivos de imagen).
 * Cada imagen se genera una sola vez y se reutiliza (ver {@code SpriteAtlas}).
 */
public final class CarArt {

    public static final int WIDTH = 256;
    public static final int HEIGHT = 150;

    private CarArt() {
    }

    /**
     * @param yaw -1..1 (negativo = gira a la izquierda)
     */
    public static BufferedImage render(int body, int stripe, int number, double yaw, boolean brake) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color bodyC = new Color(body);
        Color dark = shade(bodyC, 0.55);
        Color light = shade(bodyC, 1.25);
        Color stripeC = new Color(stripe);
        int cx = WIDTH / 2 + (int) Math.round(yaw * 6);
        int top = (int) Math.round(-yaw * 10); // desplazamiento de la parte alta (perspectiva)

        // sombra
        g.setColor(new Color(0, 0, 0, 95));
        g.fillOval(cx - 122, 126, 244, 22);

        // gomas
        g.setColor(new Color(0x111111));
        g.fillRoundRect(cx - 114 - (int) (yaw * 6), 98, 38, 44, 10, 10);
        g.fillRoundRect(cx + 76 - (int) (yaw * 6), 98, 38, 44, 10, 10);
        g.setColor(new Color(0x2C2C2C));
        g.fillRect(cx - 110 - (int) (yaw * 6), 104, 30, 4);
        g.fillRect(cx + 80 - (int) (yaw * 6), 104, 30, 4);

        // lateral visible al girar
        if (Math.abs(yaw) > 0.05) {
            int s = yaw > 0 ? -1 : 1;
            int edge = cx + s * 110;
            int ext = (int) Math.round(Math.abs(yaw) * 34) * s;
            Polygon side = new Polygon(new int[]{edge, edge + ext, edge + ext + top / 2, edge + top},
                    new int[]{122, 116, 74, 70}, 4);
            g.setColor(dark);
            g.fill(side);
            g.setColor(shade(bodyC, 0.4));
            g.fillOval(edge + ext - 14 * s - 12, 96, 24, 26); // rueda lateral
        }

        // carrocería baja
        Polygon lower = new Polygon(new int[]{cx - 110, cx + 110, cx + 104 + top, cx - 104 + top},
                new int[]{122, 122, 68, 68}, 4);
        g.setPaint(new GradientPaint(0, 68, light, 0, 122, shade(bodyC, 0.8)));
        g.fill(lower);

        // habitáculo
        Polygon cabin = new Polygon(new int[]{cx - 72 + top, cx + 72 + top, cx + 52 + top * 2, cx - 52 + top * 2},
                new int[]{70, 70, 32, 32}, 4);
        g.setPaint(new GradientPaint(0, 30, light, 0, 70, bodyC));
        g.fill(cabin);
        Polygon glass = new Polygon(new int[]{cx - 62 + top, cx + 62 + top, cx + 46 + top * 2, cx - 46 + top * 2},
                new int[]{67, 67, 38, 38}, 4);
        g.setPaint(new GradientPaint(0, 38, new Color(0x55667E), 0, 67, new Color(0x141A24)));
        g.fill(glass);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillPolygon(new int[]{cx - 30 + top * 2, cx - 14 + top * 2, cx - 34 + top, cx - 50 + top},
                new int[]{40, 40, 65, 65}, 4);

        // franjas
        g.setColor(stripeC);
        g.fillRect(cx - 20 + top, 70, 13, 52);
        g.fillRect(cx + 7 + top, 70, 13, 52);
        g.fillRect(cx - 16 + top * 2, 32, 11, 6);
        g.fillRect(cx + 5 + top * 2, 32, 11, 6);

        // alerón
        g.setColor(shade(bodyC, 0.35));
        g.fillRect(cx - 68 + top, 64, 6, 10);
        g.fillRect(cx + 62 + top, 64, 6, 10);
        g.setPaint(new GradientPaint(0, 56, shade(bodyC, 0.9), 0, 66, shade(bodyC, 0.5)));
        g.fillRoundRect(cx - 104 + top, 56, 208, 10, 6, 6);
        g.setColor(stripeC);
        g.fillRect(cx - 104 + top, 56, 12, 10);
        g.fillRect(cx + 92 + top, 56, 12, 10);

        // ópticas
        Color tail = brake ? new Color(0xFF3B30) : new Color(0x8A0E0E);
        g.setColor(new Color(0x1A1A1A));
        g.fillRoundRect(cx - 103, 79, 50, 18, 6, 6);
        g.fillRoundRect(cx + 53, 79, 50, 18, 6, 6);
        g.setColor(tail);
        g.fillRoundRect(cx - 101, 81, 46, 14, 5, 5);
        g.fillRoundRect(cx + 55, 81, 46, 14, 5, 5);
        if (brake) {
            glow(g, cx - 78, 88);
            glow(g, cx + 78, 88);
        }

        // paragolpes, patente y escapes
        g.setColor(new Color(0x262626));
        g.fillRoundRect(cx - 108, 110, 216, 12, 6, 6);
        g.setColor(new Color(0xF2F2F2));
        g.fillRoundRect(cx - 24, 90, 48, 20, 4, 4);
        g.setColor(new Color(0x111111));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        FontMetrics fm = g.getFontMetrics();
        String num = Integer.toString(number);
        g.drawString(num, cx - fm.stringWidth(num) / 2, 106);
        g.setColor(new Color(0x555555));
        g.fillOval(cx + 58, 112, 12, 9);
        g.fillOval(cx + 74, 112, 12, 9);
        g.setColor(new Color(0x111111));
        g.fillOval(cx + 60, 114, 8, 5);
        g.fillOval(cx + 76, 114, 8, 5);

        // contorno
        g.setStroke(new BasicStroke(2f));
        g.setColor(shade(bodyC, 0.3));
        g.draw(lower);
        g.draw(cabin);
        g.dispose();
        return img;
    }

    private static void glow(Graphics2D g, int x, int y) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setPaint(new RadialGradientPaint(x, y, 38, new float[]{0f, 1f},
                new Color[]{new Color(255, 60, 40, 170), new Color(255, 40, 20, 0)}));
        g.fillOval(x - 38, y - 38, 76, 76);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private static Color shade(Color c, double f) {
        return new Color(clamp(c.getRed() * f), clamp(c.getGreen() * f), clamp(c.getBlue() * f));
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }
}
