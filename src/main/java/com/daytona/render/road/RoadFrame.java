package com.daytona.render.road;

import com.daytona.domain.track.Segment;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;
import com.daytona.domain.track.TrackTheme;
import com.daytona.render.Camera;
import com.daytona.render.FogPalette;

import java.awt.AlphaComposite;

/**
 * Datos de un frame del motor pseudo-3D: proyección de cada segmento visible (de cerca
 * a lejos, acumulando curvas y alturas), niebla y paleta. Las capas lo leen para dibujar
 * (no conocen la cámara ni la pista directamente: bajo acoplamiento).
 */
public final class RoadFrame {

    public static final double SEG = TrackConstants.SEGMENT_LENGTH;
    public static final double ROAD = TrackConstants.ROAD_WIDTH;
    private static final double FOG_DENSITY = 4.5;

    private final AlphaComposite[] fogAlpha = new AlphaComposite[FogPalette.LEVELS];

    private int capacity;
    private int count;
    private Segment[] segs;
    private double[] sx1, sy1, sw1, sc1, sx2, sy2, sw2, sc2;
    private double[] centerX1, centerX2;
    private int[] fog;
    private boolean[] valid;

    private Track track;
    private TrackTheme theme;
    private FogPalette palette;
    private int width;
    private int height;
    private double basePct;
    private double camX;
    private double camY;
    private double camZ;
    private double shakeX;
    private double shakeY;

    public RoadFrame() {
        for (int i = 0; i < FogPalette.LEVELS; i++) {
            float a = 1f - (i / (float) (FogPalette.LEVELS - 1)) * 0.92f;
            fogAlpha[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
        }
    }

    private void ensure(int n) {
        if (n <= capacity) {
            return;
        }
        capacity = n;
        segs = new Segment[n];
        sx1 = new double[n];
        sy1 = new double[n];
        sw1 = new double[n];
        sc1 = new double[n];
        sx2 = new double[n];
        sy2 = new double[n];
        sw2 = new double[n];
        sc2 = new double[n];
        centerX1 = new double[n];
        centerX2 = new double[n];
        fog = new int[n];
        valid = new boolean[n];
    }

    /** Proyecta los segmentos visibles para esta cámara. */
    public void project(Track t, Camera cam, int w, int h, int drawDistance) {
        this.track = t;
        if (t.theme() != theme) {
            theme = t.theme();
            palette = new FogPalette(theme.fog());
        }
        this.width = w;
        this.height = h;
        this.count = Math.min(drawDistance, t.segmentCount() - 1);
        ensure(count);
        Segment baseSeg = t.segmentAt(cam.z());
        basePct = (t.wrap(cam.z()) % SEG) / SEG;
        int baseIndex = baseSeg.index();
        camX = cam.x();
        camY = cam.y();
        camZ = cam.z();
        shakeX = cam.shakeX();
        shakeY = cam.shakeY();
        double x = 0;
        double dx = -(baseSeg.curve() * basePct);
        double halfW = w / 2.0;
        double halfH = h / 2.0;
        for (int i = 0; i < count; i++) {
            Segment s = t.segment(baseIndex + i);
            segs[i] = s;
            double cz1 = (i - basePct) * SEG;
            double cz2 = cz1 + SEG;
            valid[i] = cz1 > 1;
            double scale1 = Camera.DEPTH / Math.max(1, cz1);
            double scale2 = Camera.DEPTH / Math.max(1, cz2);
            sc1[i] = scale1;
            sc2[i] = scale2;
            centerX1[i] = x;
            centerX2[i] = x + dx;
            sx1[i] = halfW + scale1 * (x - camX) * halfW + shakeX;
            sx2[i] = halfW + scale2 * (x + dx - camX) * halfW + shakeX;
            sy1[i] = halfH - scale1 * (s.y1() - camY) * halfH + shakeY;
            sy2[i] = halfH - scale2 * (s.y2() - camY) * halfH + shakeY;
            sw1[i] = scale1 * ROAD * halfW;
            sw2[i] = scale2 * ROAD * halfW;
            x += dx;
            dx += s.curve();
            double d = i / (double) count;
            double visible = 1 / Math.exp(d * d * FOG_DENSITY);
            fog[i] = (int) Math.round((1 - visible) * (FogPalette.LEVELS - 1));
        }
    }

    /**
     * Proyecta un punto del mundo en pantalla.
     *
     * @param relZ    distancia por delante de la cámara (unidades de mundo)
     * @param lateral desplazamiento lateral respecto del centro de la calzada
     * @param elevation altura sobre el piso en ese punto
     * @param out     out[0]=x, out[1]=y, out[2]=escala
     * @return false si el punto queda detrás de la cámara o fuera del rango dibujado
     */
    public boolean projectPoint(double relZ, double lateral, double elevation, double[] out) {
        if (relZ < 8) {
            return false;
        }
        double pos = relZ / SEG + basePct;
        int n = (int) Math.floor(pos);
        if (n < 0 || n >= count) {
            return false;
        }
        double p = pos - n;
        Segment s = segs[n];
        double cx = centerX1[n] + (centerX2[n] - centerX1[n]) * p;
        double gy = s.y1() + (s.y2() - s.y1()) * p;
        double scale = Camera.DEPTH / relZ;
        out[0] = width / 2.0 + scale * (cx + lateral - camX) * width / 2.0 + shakeX;
        out[1] = height / 2.0 - scale * (gy + elevation - camY) * height / 2.0 + shakeY;
        out[2] = scale;
        return true;
    }

    /** Distancia de un z de pista por delante de la cámara. */
    public double relativeZ(double z) {
        return track.forwardDistance(camZ, z);
    }

    // ---- accesores para las capas ----
    public int count() { return count; }
    public Segment segment(int i) { return segs[i]; }
    public boolean valid(int i) { return valid[i]; }
    public boolean frontFacing(int i) { return sy1[i] > sy2[i]; }
    public double x1(int i) { return sx1[i]; }
    public double y1(int i) { return sy1[i]; }
    public double w1(int i) { return sw1[i]; }
    public double scale1(int i) { return sc1[i]; }
    public double x2(int i) { return sx2[i]; }
    public double y2(int i) { return sy2[i]; }
    public double w2(int i) { return sw2[i]; }
    public double scale2(int i) { return sc2[i]; }
    public int fog(int i) { return fog[i]; }
    public AlphaComposite fogAlpha(int i) { return fogAlpha[fog[i]]; }
    public double basePct() { return basePct; }
    public TrackTheme theme() { return theme; }
    public FogPalette palette() { return palette; }
    public Track track() { return track; }
    public int width() { return width; }
    public int height() { return height; }

    /** Altura de pantalla de un objeto de altura {@code worldHeight} en el borde cercano del segmento. */
    public double heightAt1(int i, double worldHeight) {
        return sc1[i] * worldHeight * height / 2.0;
    }

    public double heightAt2(int i, double worldHeight) {
        return sc2[i] * worldHeight * height / 2.0;
    }

    public double camX() { return camX; }
    public double camY() { return camY; }
}
