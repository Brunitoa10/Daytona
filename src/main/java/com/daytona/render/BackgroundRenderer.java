package com.daytona.render;

import com.daytona.domain.track.TrackTheme;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Cielo y tres capas de fondo con paralaje, generadas por código y cacheadas. */
public final class BackgroundRenderer {

    private static final class Key {
        private final TrackTheme theme;
        private final int w;
        private final int h;
        private final int layer;

        Key(TrackTheme theme, int w, int h, int layer) {
            this.theme = theme;
            this.w = w;
            this.h = h;
            this.layer = layer;
        }

        public TrackTheme theme() {
            return theme;
        }

        public int w() {
            return w;
        }

        public int h() {
            return h;
        }

        public int layer() {
            return layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key other = (Key) o;
            return java.util.Objects.equals(theme, other.theme) && java.util.Objects.equals(w, other.w) && java.util.Objects.equals(h, other.h) && java.util.Objects.equals(layer, other.layer);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(theme, w, h, layer);
        }
    }

    private final Map<Key, BufferedImage> cache = new HashMap<>();

    public void render(Graphics2D g, int w, int h, TrackTheme theme, Camera cam) {
        g.drawImage(get(theme, w, h, 0), 0, 0, null);
        drawLayer(g, get(theme, w, h, 1), w, h, cam.skyOffset(), 0.12);
        drawLayer(g, get(theme, w, h, 2), w, h, cam.hillOffset(), 0.5 + 0.035);
        drawLayer(g, get(theme, w, h, 3), w, h, cam.nearOffset(), 0.5 + 0.045);
    }

    private static void drawLayer(Graphics2D g, BufferedImage img, int w, int h, double offset, double bottomFraction) {
        int y = (int) (h * bottomFraction) - img.getHeight();
        if (bottomFraction < 0.3) {
            y = (int) (h * 0.02);
        }
        int x = (int) (-offset * w);
        g.drawImage(img, x, y, null);
        g.drawImage(img, x + w, y, null);
        if (x > 0) {
            g.drawImage(img, x - w, y, null);
        }
    }

    private BufferedImage get(TrackTheme theme, int w, int h, int layer) {
        return cache.computeIfAbsent(new Key(theme, w, h, layer), k -> build(k));
    }

    private static BufferedImage build(Key k) {
        switch (k.layer) {
            case 0:
                return sky(k.theme, k.w, k.h);
            case 1:
                return clouds(k.theme, k.w, k.h);
            case 2:
                return far(k.theme, k.w, k.h);
            default:
                return near(k.theme, k.w, k.h);
        }
    }

    private static Graphics2D begin(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    private static BufferedImage sky(TrackTheme t, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = begin(img);
        g.setPaint(new GradientPaint(0, 0, new Color(t.skyTop()), 0, h * 0.55f, new Color(t.skyBottom())));
        g.fillRect(0, 0, w, h);
        Random r = new Random(7);
        if (t.night()) {
            for (int i = 0; i < w / 4; i++) {
                int a = 80 + r.nextInt(175);
                g.setColor(new Color(255, 255, 255, a));
                int s = r.nextDouble() < 0.1 ? 2 : 1;
                g.fillRect(r.nextInt(w), r.nextInt((int) (h * 0.45)), s, s);
            }
        }
        float sx = w * 0.72f;
        float sy = h * (t.night() ? 0.16f : 0.30f);
        float sr = h * 0.07f;
        g.setPaint(new RadialGradientPaint(sx, sy, sr * 4, new float[]{0f, 0.25f, 1f},
                new Color[]{new Color(t.sun()), alpha(t.sun(), 90), alpha(t.sun(), 0)}));
        g.fillOval((int) (sx - sr * 4), (int) (sy - sr * 4), (int) (sr * 8), (int) (sr * 8));
        g.setColor(new Color(t.sun()));
        g.fillOval((int) (sx - sr), (int) (sy - sr), (int) (sr * 2), (int) (sr * 2));
        g.dispose();
        return img;
    }

    private static BufferedImage clouds(TrackTheme t, int w, int h) {
        BufferedImage img = new BufferedImage(w, (int) (h * 0.35), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(3);
        int count = t.night() ? 4 : 9;
        for (int i = 0; i < count; i++) {
            int cx = r.nextInt(w);
            int cy = 10 + r.nextInt(img.getHeight() - 40);
            int cw = (int) (w * (0.08 + r.nextDouble() * 0.12));
            Color c = t.night() ? new Color(120, 110, 160, 60) : new Color(255, 255, 255, 150 + r.nextInt(80));
            g.setColor(c);
            for (int dx = -w; dx <= w; dx += w) { // repetir para que el borde empalme
                for (int p = 0; p < 5; p++) {
                    int px = cx + dx + p * cw / 5 - cw / 2;
                    int ph = cw / 3 + r.nextInt(Math.max(1, cw / 5));
                    g.fillOval(px, cy - ph / 2, cw / 2, ph);
                }
            }
        }
        g.dispose();
        return img;
    }

    private static BufferedImage far(TrackTheme t, int w, int h) {
        int lh = (int) (h * 0.3);
        BufferedImage img = new BufferedImage(w, lh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(11);
        switch (t.backdrop()) {
            case COAST_HILLS:
                ridge(g, w, lh, 0.55, 0.25, new Color(0x7B93B8), 3, 7, 0.4);
                g.setPaint(new GradientPaint(0, lh * 0.82f, new Color(0x2E6FA8), 0, lh, new Color(0x5FA0D0)));
                g.fillRect(0, (int) (lh * 0.82), w, lh);
                break;
            case CANYON:
                mesas(g, w, lh, r, new Color(0x7E4A6B), 0.45);
                break;
            case CITY_SKYLINE:
                skyline(g, w, lh, r, new Color(0x1C1838), true, 0.35);
                break;
            case ALPS:
                peaks(g, w, lh, r, new Color(0x8FA9C8), true);
                break;
            default:
                break;
        }
        g.dispose();
        return img;
    }

    private static BufferedImage near(TrackTheme t, int w, int h) {
        int lh = (int) (h * 0.2);
        BufferedImage img = new BufferedImage(w, lh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = begin(img);
        Random r = new Random(23);
        switch (t.backdrop()) {
            case COAST_HILLS:
                ridge(g, w, lh, 0.5, 0.3, new Color(0x3D7D3A), 2, 5, 0.5);
                break;
            case CANYON:
                mesas(g, w, lh, r, new Color(0xA8482B), 0.25);
                break;
            case CITY_SKYLINE:
                skyline(g, w, lh, r, new Color(0x0E0C1E), false, 0.1);
                break;
            case ALPS:
                ridge(g, w, lh, 0.45, 0.35, new Color(0x2F5245), 3, 8, 0.5);
                break;
            default:
                break;
        }
        g.dispose();
        return img;
    }

    /** Cordón de colinas periódico (empalma en los bordes). */
    private static void ridge(Graphics2D g, int w, int h, double base, double amp, Color c, int f1, int f2, double mix) {
        Polygon p = new Polygon();
        p.addPoint(0, h);
        for (int x = 0; x <= w; x += 4) {
            double t = x / (double) w * Math.PI * 2;
            double y = base + amp * ((1 - mix) * Math.sin(t * f1) + mix * Math.sin(t * f2 + 1.3)) * 0.5;
            p.addPoint(x, (int) (h * (1 - y)));
        }
        p.addPoint(w, h);
        g.setPaint(new GradientPaint(0, 0, c.brighter(), 0, h, c));
        g.fill(p);
    }

    private static void mesas(Graphics2D g, int w, int h, Random r, Color c, double minTop) {
        g.setColor(c);
        int x = 0;
        while (x < w) {
            int mw = (int) (w * (0.06 + r.nextDouble() * 0.12));
            int top = (int) (h * (minTop + r.nextDouble() * 0.35));
            int slope = mw / 6;
            g.fillPolygon(new int[]{x, x + slope, x + mw - slope, x + mw}, new int[]{h, top, top, h}, 4);
            g.setColor(c.darker());
            g.fillRect(x + slope, top, mw - 2 * slope, Math.max(2, h / 40));
            g.setColor(c);
            x += mw + (int) (w * r.nextDouble() * 0.05);
        }
        g.fillRect(0, (int) (h * 0.9), w, h);
    }

    private static void skyline(Graphics2D g, int w, int h, Random r, Color c, boolean windows, double minTop) {
        int x = 0;
        while (x < w) {
            int bw = (int) (w * (0.02 + r.nextDouble() * 0.04));
            int top = (int) (h * (minTop + r.nextDouble() * 0.5));
            if (x + bw > w) {
                bw = w - x;
            }
            g.setColor(c);
            g.fillRect(x, top, bw, h - top);
            if (windows) {
                for (int wy = top + 4; wy < h - 4; wy += 7) {
                    for (int wx = x + 3; wx < x + bw - 3; wx += 6) {
                        if (r.nextDouble() < 0.35) {
                            g.setColor(r.nextDouble() < 0.8 ? new Color(0xFFD86B) : new Color(0x6BE3FF));
                            g.fillRect(wx, wy, 2, 3);
                        }
                    }
                }
            } else if (r.nextDouble() < 0.3) {
                g.setColor(r.nextBoolean() ? new Color(0xFF2D7A) : new Color(0x00E5FF));
                g.fillRect(x + 2, top + 4, bw - 4, 3);
            }
            x += bw;
        }
    }

    private static void peaks(Graphics2D g, int w, int h, Random r, Color c, boolean snow) {
        int count = 9;
        int step = w / count;
        for (int i = -1; i <= count; i++) {
            int cx = i * step + step / 2 + r.nextInt(Math.max(1, step / 3));
            int top = (int) (h * (0.05 + r.nextDouble() * 0.35));
            int half = (int) (step * (0.7 + r.nextDouble() * 0.5));
            g.setPaint(new GradientPaint(cx - half, 0, c, cx + half, 0, c.darker()));
            g.fillPolygon(new int[]{cx - half, cx, cx + half}, new int[]{h, top, h}, 3);
            if (snow) {
                int sh = (h - top) / 3;
                g.setColor(new Color(0xF4F8FC));
                g.fillPolygon(new int[]{cx - half * sh / (h - top), cx, cx + half * sh / (h - top), cx + half / 8, cx - half / 7},
                        new int[]{top + sh, top, top + sh, top + sh - sh / 4, top + sh - sh / 5}, 5);
            }
        }
    }

    private static Color alpha(int rgb, int a) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, a);
    }

    /** Mantener la API simple: los temas se cachean por valor. */
    public void clear() {
        cache.clear();
    }

}
