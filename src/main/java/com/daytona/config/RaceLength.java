package com.daytona.config;

/** Multiplica la cantidad de vueltas por defecto de cada pista. */
public enum RaceLength {
    SHORT("Corta", 0.5),
    NORMAL("Normal", 1.0),
    LONG("Larga", 2.0);

    private final String label;
    private final double factor;

    private RaceLength(String label, double factor) {
        this.label = label;
        this.factor = factor;
    }

    public String label() {
        return label;
    }

    public int apply(int defaultLaps) {
        return Math.max(1, (int) Math.round(defaultLaps * factor));
    }
}
