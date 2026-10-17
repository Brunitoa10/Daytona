package com.daytona.input;

/** Tipo de palanca para la caja manual. */
public enum ShifterType {
    SEQUENTIAL("Secuencial / levas"),
    H_PATTERN("Palanca H (1-6)");

    private final String label;

    ShifterType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
