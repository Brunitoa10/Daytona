package com.daytona.domain.car;

public enum TransmissionType {
    AUTOMATIC("Automática (AT)", "AT"),
    MANUAL("Manual (MT)", "MT");

    private final String label;
    private final String shortLabel;

    private TransmissionType(String label, String shortLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
    }

    public String label() {
        return label;
    }

    public String shortLabel() {
        return shortLabel;
    }
}
