package com.daytona.domain.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tramo recto de pista; la curvatura y la altura se acumulan al proyectar. */
public final class Segment {

    private final int index;
    private final double curve;
    private final double y1;
    private final double y2;
    private final boolean tunnel;
    private final boolean walls;
    private final List<TrackSprite> sprites = new ArrayList<>(2);
    private final List<TrackSprite> readOnly = Collections.unmodifiableList(sprites);

    public Segment(int index, double curve, double y1, double y2, boolean tunnel, boolean walls) {
        this.index = index;
        this.curve = curve;
        this.y1 = y1;
        this.y2 = y2;
        this.tunnel = tunnel;
        this.walls = walls;
    }

    public void addSprite(TrackSprite s) {
        sprites.add(s);
    }

    public int index() { return index; }
    public double curve() { return curve; }
    public double y1() { return y1; }
    public double y2() { return y2; }
    public boolean tunnel() { return tunnel; }
    public boolean walls() { return walls || tunnel; }
    public List<TrackSprite> sprites() { return readOnly; }

    public double z1() {
        return index * TrackConstants.SEGMENT_LENGTH;
    }

    /** Altura interpolada dentro del segmento (p = 0..1). */
    public double heightAt(double p) {
        return y1 + (y2 - y1) * p;
    }
}
