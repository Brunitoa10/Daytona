package com.daytona.domain.track;

/** Escalas del mundo pseudo-3D. */
public final class TrackConstants {

    /** Largo de un segmento de pista (unidades de mundo). */
    public static final double SEGMENT_LENGTH = 200;
    /** Media anchura de la calzada: x = ±1 son los bordes. */
    public static final double ROAD_WIDTH = 2000;
    /** Segmentos por franja de piano (rumble). */
    public static final int RUMBLE_LENGTH = 3;
    /** Carriles pintados. */
    public static final int LANES = 4;
    /** Metros por unidad de mundo (para mostrar km/h y distancias). */
    public static final double METERS_PER_UNIT = 0.0075;

    private TrackConstants() {
    }

    public static double kmhToUnits(double kmh) {
        return kmh / 3.6 / METERS_PER_UNIT;
    }

    public static double unitsToKmh(double units) {
        return units * METERS_PER_UNIT * 3.6;
    }
}
