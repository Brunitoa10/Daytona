package com.daytona.render.road;

import com.daytona.domain.track.Segment;
import com.daytona.domain.track.TrackConstants;
import com.daytona.domain.track.TrackTheme;
import com.daytona.render.FogPalette;

import java.awt.Graphics2D;

/** Pasto, pianos, asfalto, líneas de carril, largada a cuadros y muros bajos. */
public final class GroundLayer implements SegmentLayer {

    private static final double WALL_HEIGHT = 420;
    private static final double WALL_OFFSET = 1.27;

    private final Polygons poly = new Polygons();

    @Override
    public void paint(Graphics2D g, RoadFrame f, int i) {
        if (!f.frontFacing(i)) {
            return;
        }
        Segment s = f.segment(i);
        TrackTheme t = f.theme();
        FogPalette pal = f.palette();
        int level = f.fog(i);
        boolean alt = (s.index() / TrackConstants.RUMBLE_LENGTH) % 2 == 0;
        boolean tunnel = s.tunnel();
        int grass = tunnel ? t.tunnelWall() : (alt ? t.grassLight() : t.grassDark());
        int rumble = alt ? t.rumbleLight() : t.rumbleDark();
        int road = alt ? t.roadLight() : t.roadDark();
        if (tunnel) {
            road = FogPalette.darker(road, 0.35);
            rumble = FogPalette.darker(rumble, 0.4);
        }
        double x1 = f.x1(i), y1 = f.y1(i), w1 = f.w1(i);
        double x2 = f.x2(i), y2 = f.y2(i), w2 = f.w2(i);

        int top = (int) Math.round(y2);
        int bottom = (int) Math.round(y1);
        g.setColor(pal.get(grass, level));
        g.fillRect(0, top, f.width(), Math.max(1, bottom - top));

        double r1 = w1 / 6.5, r2 = w2 / 6.5;
        g.setColor(pal.get(rumble, level));
        poly.quad(g, x1 - w1 - r1, y1, x1 - w1, y1, x2 - w2, y2, x2 - w2 - r2, y2);
        poly.quad(g, x1 + w1 + r1, y1, x1 + w1, y1, x2 + w2, y2, x2 + w2 + r2, y2);
        g.setColor(pal.get(road, level));
        poly.quad(g, x1 - w1, y1, x1 + w1, y1, x2 + w2, y2, x2 - w2, y2);

        if (s.index() <= 1) {
            checker(g, pal, s.index(), x1, y1, w1, x2, y2, w2, level);
        } else if (alt) {
            lanes(g, pal.get(t.lane(), level), x1, y1, w1, x2, y2, w2);
        }
        if (s.walls() && !tunnel) {
            walls(g, f, i, alt, level);
        }
    }

    private void lanes(Graphics2D g, java.awt.Color c, double x1, double y1, double w1, double x2, double y2, double w2) {
        g.setColor(c);
        int lanes = TrackConstants.LANES;
        double l1 = w1 / 38, l2 = w2 / 38;
        for (int lane = 1; lane < lanes; lane++) {
            double k = -1 + 2.0 * lane / lanes;
            double lx1 = x1 + w1 * k;
            double lx2 = x2 + w2 * k;
            poly.quad(g, lx1 - l1, y1, lx1 + l1, y1, lx2 + l2, y2, lx2 - l2, y2);
        }
    }

    private void walls(Graphics2D g, RoadFrame f, int i, boolean alt, int level) {
        TrackTheme t = f.theme();
        double x1 = f.x1(i), y1 = f.y1(i), w1 = f.w1(i);
        double x2 = f.x2(i), y2 = f.y2(i), w2 = f.w2(i);
        double h1 = f.heightAt1(i, WALL_HEIGHT);
        double h2 = f.heightAt2(i, WALL_HEIGHT);
        int wall = alt ? t.wall() : FogPalette.darker(t.wall(), 0.18);
        g.setColor(f.palette().get(wall, level));
        double a1 = x1 - w1 * WALL_OFFSET, a2 = x2 - w2 * WALL_OFFSET;
        double b1 = x1 + w1 * WALL_OFFSET, b2 = x2 + w2 * WALL_OFFSET;
        poly.quad(g, a1, y1, a2, y2, a2, y2 - h2, a1, y1 - h1);
        poly.quad(g, b1, y1, b2, y2, b2, y2 - h2, b1, y1 - h1);
        g.setColor(f.palette().get(alt ? t.rumbleDark() : t.rumbleLight(), level));
        poly.quad(g, a1, y1 - h1, a2, y2 - h2, a2, y2 - h2 * 0.8, a1, y1 - h1 * 0.8);
        poly.quad(g, b1, y1 - h1, b2, y2 - h2, b2, y2 - h2 * 0.8, b1, y1 - h1 * 0.8);
    }

    private void checker(Graphics2D g, FogPalette pal, int index, double x1, double y1, double w1,
                         double x2, double y2, double w2, int level) {
        int cells = 12;
        for (int c = 0; c < cells; c++) {
            g.setColor(pal.get(((c + index) % 2 == 0) ? 0xF5F5F5 : 0x151515, level));
            double f0 = -1 + 2.0 * c / cells;
            double f1 = -1 + 2.0 * (c + 1) / cells;
            poly.quad(g, x1 + w1 * f0, y1, x1 + w1 * f1, y1, x2 + w2 * f1, y2, x2 + w2 * f0, y2);
        }
    }
}
