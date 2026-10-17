package com.daytona.render;

import com.daytona.domain.race.Racer;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Minimapa con el contorno de la pista y los autos como puntos. */
public final class MinimapRenderer {

    private static final class Key {
        private final Track track;
        private final int size;

        Key(Track track, int size) {
            this.track = track;
            this.size = size;
        }

        public Track track() {
            return track;
        }

        public int size() {
            return size;
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
            return java.util.Objects.equals(track, other.track) && java.util.Objects.equals(size, other.size);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(track, size);
        }
    }

    private final Map<Key, BufferedImage> cache = new HashMap<>();

    public void render(Graphics2D g, Track track, double x, double y, int size, List<Racer> racers, double ghostZ) {
        g.drawImage(outline(track, size), (int) x, (int) y, null);
        float[][] pts = track.minimap();
        int pad = size / 10;
        int inner = size - pad * 2;
        if (ghostZ >= 0) {
            dot(g, pts, track, ghostZ, x + pad, y + pad, inner, new Color(255, 255, 255, 120), size / 18);
        }
        for (Racer r : racers) {
            if (!r.isPlayer()) {
                dot(g, pts, track, r.car().z(), x + pad, y + pad, inner, new Color(r.car().spec().bodyRgb()), size / 22);
            }
        }
        for (Racer r : racers) {
            if (r.isPlayer()) {
                dot(g, pts, track, r.car().z(), x + pad, y + pad, inner, UiKit.ACCENT, size / 13);
            }
        }
    }

    private static void dot(Graphics2D g, float[][] pts, Track track, double z, double ox, double oy, int inner, Color c, int r) {
        int i = Math.floorMod((int) (z / TrackConstants.SEGMENT_LENGTH), pts.length);
        int px = (int) (ox + pts[i][0] * inner);
        int py = (int) (oy + pts[i][1] * inner);
        g.setColor(Color.BLACK);
        g.fillOval(px - r - 1, py - r - 1, r * 2 + 2, r * 2 + 2);
        g.setColor(c);
        g.fillOval(px - r, py - r, r * 2, r * 2);
    }

    public BufferedImage outline(Track track, int size) {
        return cache.computeIfAbsent(new Key(track, size), k -> build(track, size));
    }

    private static BufferedImage build(Track track, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        UiKit.smooth(g);
        float[][] pts = track.minimap();
        int pad = size / 10;
        int inner = size - pad * 2;
        Path2D.Float path = new Path2D.Float();
        for (int i = 0; i < pts.length; i += 2) {
            float px = pad + pts[i][0] * inner;
            float py = pad + pts[i][1] * inner;
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        g.setStroke(new BasicStroke(size / 16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0, 0, 0, 170));
        g.draw(path);
        g.setStroke(new BasicStroke(size / 34f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 220));
        g.draw(path);
        int sx = (int) (pad + pts[0][0] * inner);
        int sy = (int) (pad + pts[0][1] * inner);
        g.setColor(UiKit.ACCENT2);
        g.fillRect(sx - size / 30, sy - size / 30, size / 15, size / 15);
        g.dispose();
        return img;
    }
}
