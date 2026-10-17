package com.daytona.config;

/** Resolución interna de render; luego se escala a la ventana. Más baja = más FPS. */
public enum Resolution {
    R640("640 x 360", 640, 360),
    R960("960 x 540", 960, 540),
    R1280("1280 x 720", 1280, 720),
    R1600("1600 x 900", 1600, 900),
    R1920("1920 x 1080", 1920, 1080);

    private final String label;
    private final int width;
    private final int height;

    Resolution(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    public String label() {
        return label;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
