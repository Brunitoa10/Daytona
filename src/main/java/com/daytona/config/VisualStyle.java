package com.daytona.config;

/** Familias visuales del juego (cada una la arma una fábrica de temas). */
public enum VisualStyle {
    ARCADE_94("Arcade 1994 (CRT)"),
    ARCADE_HD("Arcade HD"),
    CLASSIC_2D("Clásico 2D (sprites)");

    private final String label;

    VisualStyle(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
