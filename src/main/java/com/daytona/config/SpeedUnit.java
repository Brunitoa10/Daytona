package com.daytona.config;

public enum SpeedUnit {
    KMH("km/h", 1.0),
    MPH("mph", 0.621371);

    private final String label;
    private final double fromKmh;

    private SpeedUnit(String label, double fromKmh) {
        this.label = label;
        this.fromKmh = fromKmh;
    }

    public String label() {
        return label;
    }

    public double convert(double kmh) {
        return kmh * fromKmh;
    }
}
