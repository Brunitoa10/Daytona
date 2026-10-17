package com.daytona.render.road;

import java.awt.Graphics2D;

/** Relleno de cuadriláteros sin crear objetos por llamada. */
public final class Polygons {

    private final int[] px = new int[4];
    private final int[] py = new int[4];

    public void quad(Graphics2D g, double ax, double ay, double bx, double by,
                     double cx, double cy, double dx, double dy) {
        px[0] = (int) Math.round(ax);
        py[0] = (int) Math.round(ay);
        px[1] = (int) Math.round(bx);
        py[1] = (int) Math.round(by);
        px[2] = (int) Math.round(cx);
        py[2] = (int) Math.round(cy);
        px[3] = (int) Math.round(dx);
        py[3] = (int) Math.round(dy);
        g.fillPolygon(px, py, 4);
    }
}
