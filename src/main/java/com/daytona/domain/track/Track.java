package com.daytona.domain.track;

import java.util.List;

/** Pista inmutable lista para correr y dibujar. */
public final class Track {

    private final TrackInfo info;
    private final Segment[] segments;
    private final TrackTheme theme;
    private final int[] checkpoints;
    private final boolean mirror;
    private final float[][] minimap;

    public Track(TrackInfo info, List<Segment> segments, TrackTheme theme, int[] checkpoints, boolean mirror, float[][] minimap) {
        this.info = info;
        this.segments = segments.toArray(new Segment[0]);
        this.theme = theme;
        this.checkpoints = checkpoints.clone();
        this.mirror = mirror;
        this.minimap = minimap;
    }

    public TrackInfo info() { return info; }
    public String id() { return info.id(); }
    public String name() { return info.name(); }
    public TrackTheme theme() { return theme; }
    public boolean mirror() { return mirror; }
    public int segmentCount() { return segments.length; }

    public double length() {
        return segments.length * TrackConstants.SEGMENT_LENGTH;
    }

    public Segment segment(int index) {
        int n = segments.length;
        return segments[((index % n) + n) % n];
    }

    public Segment segmentAt(double z) {
        return segment((int) Math.floor(z / TrackConstants.SEGMENT_LENGTH));
    }

    /** Altura del piso en una posición z. */
    public double heightAt(double z) {
        Segment s = segmentAt(z);
        double p = (wrap(z) % TrackConstants.SEGMENT_LENGTH) / TrackConstants.SEGMENT_LENGTH;
        return s.heightAt(p);
    }

    public double wrap(double z) {
        double len = length();
        double r = z % len;
        return r < 0 ? r + len : r;
    }

    /** Distancia hacia adelante de a hasta b, considerando la vuelta. */
    public double forwardDistance(double fromZ, double toZ) {
        return wrap(toZ - fromZ);
    }

    /** Índices de segmento de los checkpoints (sin incluir la meta). */
    public int[] checkpoints() {
        return checkpoints.clone();
    }

    /** Puntos normalizados [0,1] del minimapa (x,y). */
    public float[][] minimap() {
        return minimap;
    }

    /** Curvatura absoluta máxima de los próximos n segmentos. */
    public double maxCurveAhead(double z, int n) {
        int start = (int) Math.floor(z / TrackConstants.SEGMENT_LENGTH);
        double max = 0;
        for (int i = 0; i <= n; i++) {
            max = Math.max(max, Math.abs(segment(start + i).curve()));
        }
        return max;
    }

    /** Suma de curvatura de los próximos n segmentos (para la IA). */
    public double curvatureAhead(double z, int n) {
        int start = (int) Math.floor(z / TrackConstants.SEGMENT_LENGTH);
        double sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += segment(start + i).curve();
        }
        return sum;
    }
}
