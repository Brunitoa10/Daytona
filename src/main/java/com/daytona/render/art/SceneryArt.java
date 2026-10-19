package com.daytona.render.art;

import com.daytona.domain.track.SpriteKind;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Objetos de escenario dibujados por código. */
public final class SceneryArt {

    private static final String[] BILLBOARD_TEXT = {"DAYTONA", "TURBO OIL", "NITRO COLA", "RACE FM 98.5"};
    private static final int[][] BILLBOARD_COLORS = {
            {0xF5A623, 0x1D1D1D}, {0x1565C0, 0xFFFFFF}, {0xD7261E, 0xFFFFFF}, {0x2E7D32, 0xFFEB3B}};
    private static final int[] BUILDING_COLORS = {0x5D6D7E, 0x7B6D5D, 0x4A5A6A, 0x8A7F73};

    private SceneryArt() {
    }

    public static BufferedImage render(SpriteKind kind, int variant, boolean night) {
        switch (kind) {
            case PALM:
                return palm(variant);
            case PINE:
                return pine(variant, false);
            case SNOW_PINE:
                return pine(variant, true);
            case TREE:
                return tree(variant);
            case CACTUS:
                return cactus(variant);
            case ROCK:
                return rock(variant, 0x8A7766);
            case CLIFF:
                return cliff(variant);
            case BUILDING:
                return building(variant, night, 360, 560);
            case TOWER:
                return building(variant + 1, night, 240, 820);
            case LAMP:
                return lamp(night);
            case BILLBOARD:
                return billboard(variant, night);
            case GRANDSTAND:
                return grandstand(variant);
            case TIRES:
                return tires();
            case FLAG:
                return flag(variant);
            case CHEVRON_LEFT:
                return chevron(true);
            case CHEVRON_RIGHT:
                return chevron(false);
            case GANTRY:
                return gantry("START  ·  FINISH", 0x101010, 0xFFFFFF, true);
            case CHECKPOINT:
                return gantry("CHECKPOINT", 0x0D47A1, 0xFFD600, false);
            default:
                throw new IllegalStateException(String.valueOf(kind));
        }
    }

    private static Graphics2D begin(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    private static BufferedImage palm(int v) {
        BufferedImage img = new BufferedImage(200, 340, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        double lean = (v % 2 == 0 ? 1 : -1) * 18;
        for (int i = 0; i < 14; i++) {
            double t = i / 13.0;
            int x = (int) (100 + lean * t * t - 9);
            int y = 330 - (int) (t * 230);
            g.setColor(i % 2 == 0 ? new Color(0x8B6A43) : new Color(0x76583A));
            g.fillRoundRect(x, y - 18, 18 - (int) (t * 5), 20, 6, 6);
        }
        int tx = (int) (100 + lean);
        int ty = 100;
        Color[] greens = {new Color(0x2E8B3A), new Color(0x3FA34D), new Color(0x267A31)};
        for (int i = 0; i < 9; i++) {
            double a = Math.PI * (0.05 + i / 8.0 * 0.9) + Math.PI;
            Path2D.Double frond = new Path2D.Double();
            frond.moveTo(tx, ty);
            double ex = tx + Math.cos(a) * 95;
            double ey = ty + Math.sin(a) * 50 + 45;
            frond.quadTo(tx + Math.cos(a) * 60, ty + Math.sin(a) * 70, ex, ey);
            frond.quadTo(tx + Math.cos(a) * 50, ty + Math.sin(a) * 40, tx, ty + 8);
            g.setColor(greens[i % greens.length]);
            g.fill(frond);
        }
        g.setColor(new Color(0x5A3E22));
        g.fillOval(tx - 10, ty - 4, 20, 18);
        g.dispose();
        return img;
    }

    private static BufferedImage pine(int v, boolean snow) {
        BufferedImage img = new BufferedImage(200, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0x5B3A1E));
        g.fillRect(90, 270, 20, 50);
        Color c1 = snow ? new Color(0x1F4D3A) : new Color(0x1E5B2E);
        Color c2 = snow ? new Color(0x2A6249) : new Color(0x2A7A3D);
        for (int i = 0; i < 4; i++) {
            int y = 40 + i * 58;
            int half = 40 + i * 20;
            Polygon p = new Polygon(new int[]{100, 100 + half, 100 - half}, new int[]{y - 30, y + 60, y + 60}, 3);
            g.setPaint(new GradientPaint(100 - half, 0, c1, 100 + half, 0, c2));
            g.fill(p);
            if (snow) {
                g.setColor(new Color(0xF4F8FC));
                g.fillPolygon(new int[]{100, 100 + half / 2, 100 + half / 4, 100, 100 - half / 4, 100 - half / 2},
                        new int[]{y - 30, y + 10, y + 4, y + 12, y + 4, y + 10}, 6);
                g.fillRect(100 - half, y + 54, half * 2, 6);
            }
        }
        g.dispose();
        return img;
    }

    private static BufferedImage tree(int v) {
        BufferedImage img = new BufferedImage(220, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0x5B3A1E));
        g.fillRect(98, 170, 24, 90);
        Random r = new Random(v);
        for (int i = 0; i < 9; i++) {
            int x = 30 + r.nextInt(100);
            int y = 20 + r.nextInt(110);
            int s = 70 + r.nextInt(40);
            g.setColor(i % 2 == 0 ? new Color(0x2F7D32) : new Color(0x3E9B42));
            g.fillOval(x, y, s, s);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage cactus(int v) {
        BufferedImage img = new BufferedImage(140, 220, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Color c = new Color(0x3C7A3A);
        Color d = new Color(0x2C5E2B);
        g.setColor(c);
        g.fillRoundRect(55, 20, 30, 200, 30, 30);
        g.fillRoundRect(15, 80, 24, 70, 24, 24);
        g.fillRoundRect(15, 130, 50, 20, 20, 20);
        if (v % 2 == 0) {
            g.fillRoundRect(100, 60, 24, 70, 24, 24);
            g.fillRoundRect(75, 110, 49, 20, 20, 20);
        }
        g.setColor(d);
        g.fillRect(66, 30, 4, 185);
        g.dispose();
        return img;
    }

    private static BufferedImage rock(int v, int base) {
        BufferedImage img = new BufferedImage(260, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(v + 11);
        Polygon p = new Polygon();
        for (int i = 0; i <= 10; i++) {
            double a = Math.PI + i / 10.0 * Math.PI;
            p.addPoint(130 + (int) (Math.cos(a) * (110 + r.nextInt(18))), 158 + (int) (Math.sin(a) * (120 + r.nextInt(30))));
        }
        g.setPaint(new GradientPaint(0, 20, new Color(FogShades.lighter(base)), 260, 160, new Color(FogShades.darker(base))));
        g.fill(p);
        g.setColor(new Color(0, 0, 0, 50));
        g.setStroke(new BasicStroke(3));
        g.drawLine(90, 60, 120, 110);
        g.drawLine(160, 50, 150, 100);
        g.dispose();
        return img;
    }

    private static BufferedImage cliff(int v) {
        BufferedImage img = new BufferedImage(400, 420, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(v * 7 + 3);
        Polygon p = new Polygon();
        p.addPoint(0, 420);
        for (int i = 0; i <= 8; i++) {
            p.addPoint(i * 50, 20 + r.nextInt(80));
        }
        p.addPoint(400, 420);
        g.setPaint(new GradientPaint(0, 0, new Color(0xC2603A), 0, 420, new Color(0x7A3220)));
        g.fill(p);
        g.setClip(p);
        for (int y = 60; y < 420; y += 34 + r.nextInt(16)) {
            g.setColor(new Color(0, 0, 0, 30 + r.nextInt(30)));
            g.fillRect(0, y, 400, 6 + r.nextInt(6));
            g.setColor(new Color(255, 200, 150, 25));
            g.fillRect(0, y - 4, 400, 3);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage building(int v, boolean night, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(v * 13 + w);
        int base = BUILDING_COLORS[Math.floorMod(v, BUILDING_COLORS.length)];
        Color body = night ? new Color(FogShades.darker(base)) : new Color(base);
        g.setPaint(new GradientPaint(0, 0, body, w, 0, new Color(FogShades.darker(body.getRGB() & 0xFFFFFF))));
        g.fillRect(0, 0, w, h);
        int cols = w / 40;
        int rows = h / 46;
        for (int row = 1; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color win;
                if (night) {
                    win = r.nextDouble() < 0.55 ? new Color(0xFFD86B) : new Color(0x1C2230);
                } else {
                    win = r.nextDouble() < 0.2 ? new Color(0x9EC7E8) : new Color(0x5F86A8);
                }
                g.setColor(win);
                g.fillRect(12 + col * 40, row * 46, 22, 26);
            }
        }
        if (night) {
            g.setColor(new Color(v % 2 == 0 ? 0xFF2D7A : 0x00E5FF));
            g.fillRect(0, 6, w, 6);
        }
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(0, 0, w, 10);
        g.dispose();
        return img;
    }

    private static BufferedImage lamp(boolean night) {
        BufferedImage img = new BufferedImage(120, 420, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        if (night) {
            g.setPaint(new RadialGradientPaint(60, 30, 60, new float[]{0f, 1f},
                    new Color[]{new Color(255, 230, 160, 200), new Color(255, 220, 140, 0)}));
            g.fillOval(0, -30, 120, 120);
        }
        g.setColor(new Color(0x4A4F57));
        g.fillRect(55, 30, 10, 390);
        g.fillRoundRect(30, 20, 60, 16, 8, 8);
        g.setColor(night ? new Color(0xFFF2C0) : new Color(0xD9D9D9));
        g.fillRoundRect(36, 32, 48, 8, 6, 6);
        g.dispose();
        return img;
    }

    private static BufferedImage billboard(int v, boolean night) {
        BufferedImage img = new BufferedImage(400, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        int idx = Math.floorMod(v, BILLBOARD_TEXT.length);
        g.setColor(new Color(0x3A3A3A));
        g.fillRect(60, 150, 16, 110);
        g.fillRect(324, 150, 16, 110);
        Color bg = new Color(BILLBOARD_COLORS[idx][0]);
        Color fg = new Color(BILLBOARD_COLORS[idx][1]);
        g.setColor(new Color(0x222222));
        g.fillRect(4, 4, 392, 156);
        g.setPaint(new GradientPaint(0, 8, bg, 0, 156, new Color(FogShades.darker(bg.getRGB() & 0xFFFFFF))));
        g.fillRect(10, 10, 380, 144);
        g.setColor(fg);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 50));
        FontMetrics fm = g.getFontMetrics();
        String t = BILLBOARD_TEXT[idx];
        if (fm.stringWidth(t) > 360) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 40));
            fm = g.getFontMetrics();
        }
        g.drawString(t, 200 - fm.stringWidth(t) / 2, 100);
        if (night) {
            g.setColor(new Color(255, 255, 255, 60));
            g.setStroke(new BasicStroke(6));
            g.drawRect(10, 10, 380, 144);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage grandstand(int v) {
        BufferedImage img = new BufferedImage(600, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0xBFC5CC));
        g.fillPolygon(new int[]{0, 600, 600, 0}, new int[]{70, 40, 260, 260}, 4);
        Random r = new Random(v + 99);
        int[] crowd = {0xE53935, 0x1E88E5, 0xFDD835, 0xFFFFFF, 0x43A047, 0xFB8C00, 0x8E24AA, 0x222222};
        for (int row = 0; row < 8; row++) {
            int y = 80 + row * 22;
            g.setColor(new Color(0x8E959C));
            g.fillRect(0, y + 14, 600, 6);
            for (int x = 6; x < 594; x += 9) {
                if (r.nextDouble() < 0.85) {
                    g.setColor(new Color(crowd[r.nextInt(crowd.length)]));
                    g.fillOval(x, y + r.nextInt(3), 7, 9);
                }
            }
        }
        g.setColor(new Color(0x37474F));
        g.fillPolygon(new int[]{0, 600, 600, 0}, new int[]{30, 0, 30, 60}, 4);
        g.fillRect(20, 40, 8, 40);
        g.fillRect(570, 20, 8, 40);
        g.dispose();
        return img;
    }

    private static BufferedImage tires() {
        BufferedImage img = new BufferedImage(140, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        for (int row = 0; row < 4; row++) {
            for (int c = 0; c < 2; c++) {
                g.setColor(new Color(0x151515));
                g.fillRoundRect(8 + c * 64, 92 - row * 28, 60, 28, 18, 18);
                g.setColor(row % 2 == 0 ? new Color(0xE53935) : new Color(0xF5F5F5));
                g.fillRect(14 + c * 64, 102 - row * 28, 48, 6);
            }
        }
        g.dispose();
        return img;
    }

    private static BufferedImage flag(int v) {
        BufferedImage img = new BufferedImage(90, 260, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0xCCCCCC));
        g.fillRect(8, 10, 6, 250);
        int[] colors = {0xD7261E, 0x1565C0, 0xFDD835, 0x43A047};
        if (v % 2 == 0) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 5; x++) {
                    g.setColor((x + y) % 2 == 0 ? Color.BLACK : Color.WHITE);
                    g.fillRect(14 + x * 14, 12 + y * 13, 14, 13);
                }
            }
        } else {
            g.setColor(new Color(colors[Math.floorMod(v, colors.length)]));
            g.fillPolygon(new int[]{14, 86, 14}, new int[]{12, 38, 64}, 3);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage chevron(boolean left) {
        BufferedImage img = new BufferedImage(180, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0x444444));
        g.fillRect(40, 90, 10, 60);
        g.fillRect(130, 90, 10, 60);
        g.setColor(new Color(0x111111));
        g.fillRect(4, 4, 172, 92);
        g.setColor(new Color(0xFFD600));
        g.fillRect(8, 8, 164, 84);
        g.setColor(new Color(0x111111));
        for (int i = 0; i < 3; i++) {
            int x = 22 + i * 48;
            int[] xs = left ? new int[]{x + 44, x + 28, x, x + 28, x + 44, x + 16}
                    : new int[]{x, x + 16, x + 44, x + 16, x, x + 28};
            g.fillPolygon(xs, new int[]{14, 14, 50, 86, 86, 50}, 6);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage gantry(String text, int bg, int fg, boolean checker) {
        BufferedImage img = new BufferedImage(560, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        g.setColor(new Color(0x3C4147));
        g.fillRect(0, 40, 26, 320);
        g.fillRect(534, 40, 26, 320);
        g.setColor(new Color(0x222222));
        g.fillRect(0, 30, 560, 100);
        g.setColor(new Color(bg));
        g.fillRect(6, 36, 548, 88);
        if (checker) {
            for (int x = 0; x < 548; x += 16) {
                for (int y = 0; y < 2; y++) {
                    g.setColor(((x / 16) + y) % 2 == 0 ? Color.WHITE : Color.BLACK);
                    g.fillRect(6 + x, 36 + y * 12, 16, 12);
                    g.fillRect(6 + x, 100 + y * 12, 16, 12);
                }
            }
        }
        g.setColor(new Color(fg));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, checker ? 36 : 46));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, 280 - fm.stringWidth(text) / 2, checker ? 92 : 97);
        g.dispose();
        return img;
    }

    /** Utilidades de tono sin depender del paquete render. */
    private static final class FogShades {
        private FogShades() {
        }

        private static int darker(int rgb) {
            return scale(rgb, 0.62);
        }

        private static int lighter(int rgb) {
            return scale(rgb, 1.25);
        }

        private static int scale(int rgb, double f) {
            int r = (int) Math.min(255, ((rgb >> 16) & 0xFF) * f);
            int g = (int) Math.min(255, ((rgb >> 8) & 0xFF) * f);
            int b = (int) Math.min(255, (rgb & 0xFF) * f);
            return (r << 16) | (g << 8) | b;
        }
    }
}
