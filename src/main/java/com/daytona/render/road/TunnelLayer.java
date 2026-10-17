package com.daytona.render.road;

import com.daytona.domain.track.TrackTheme;
import com.daytona.render.FogPalette;

import java.awt.Graphics2D;

/** Paredes, techo, luces y boca de los túneles. */
public final class TunnelLayer implements SegmentLayer {

    private static final double TUNNEL_HEIGHT = 2900;
    private static final double WIDTH = 1.3;

    private final Polygons poly = new Polygons();

    @Override
    public void paint(Graphics2D g, RoadFrame f, int i) {
        if (!f.frontFacing(i) || !f.segment(i).tunnel()) {
            return;
        }
        TrackTheme t = f.theme();
        FogPalette pal = f.palette();
        int level = f.fog(i);
        double x1 = f.x1(i), y1 = f.y1(i), w1 = f.w1(i);
        double x2 = f.x2(i), y2 = f.y2(i), w2 = f.w2(i);
        double h1 = f.heightAt1(i, TUNNEL_HEIGHT);
        double h2 = f.heightAt2(i, TUNNEL_HEIGHT);
        double a1 = x1 - w1 * WIDTH, a2 = x2 - w2 * WIDTH;
        double b1 = x1 + w1 * WIDTH, b2 = x2 + w2 * WIDTH;
        boolean alt = (f.segment(i).index() / 2) % 2 == 0;
        int wall = alt ? t.tunnelWall() : FogPalette.darker(t.tunnelWall(), 0.15);
        g.setColor(pal.get(wall, level));
        poly.quad(g, a1, y1, a2, y2, a2, y2 - h2, a1, y1 - h1);
        poly.quad(g, b1, y1, b2, y2, b2, y2 - h2, b1, y1 - h1);
        g.setColor(pal.get(t.tunnelCeiling(), level));
        poly.quad(g, a1, y1 - h1, b1, y1 - h1, b2, y2 - h2, a2, y2 - h2);
        if (f.segment(i).index() % 12 == 0) {
            g.setColor(pal.get(0xFFE9A8, level));
            double lw1 = w1 * 0.25, lw2 = w2 * 0.25;
            poly.quad(g, x1 - lw1, y1 - h1 * 0.98, x1 + lw1, y1 - h1 * 0.98, x2 + lw2, y2 - h2 * 0.98, x2 - lw2, y2 - h2 * 0.98);
        }
        boolean entrance = i > 0 && !f.segment(i - 1).tunnel();
        if (entrance) {
            portal(g, f, x1, y1, w1, h1, level);
        }
    }

    /** Frente de la montaña / puente en la boca del túnel. */
    private void portal(Graphics2D g, RoadFrame f, double x, double y, double rw, double th, int level) {
        TrackTheme t = f.theme();
        double top = y - th;
        double faceTop = y - th * 2.4;
        double left = x - rw * 7;
        double right = x + rw * 7;
        double inL = x - rw * WIDTH;
        double inR = x + rw * WIDTH;
        g.setColor(f.palette().get(FogPalette.darker(t.wall(), 0.25), level));
        poly.quad(g, left, top, right, top, right - rw * 1.5, faceTop, left + rw * 1.5, faceTop);
        poly.quad(g, left, y, inL, y, inL, top, left, top);
        poly.quad(g, inR, y, right, y, right, top, inR, top);
        g.setColor(f.palette().get(FogPalette.darker(t.wall(), 0.5), level));
        poly.quad(g, inL - rw * 0.15, top - th * 0.12, inR + rw * 0.15, top - th * 0.12, inR + rw * 0.15, top,
                inL - rw * 0.15, top);
    }
}
