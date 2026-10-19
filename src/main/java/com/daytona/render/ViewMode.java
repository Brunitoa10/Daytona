package com.daytona.render;

/** Cámaras disponibles durante la carrera. */
public enum ViewMode {
    CHASE_FAR("Lejana", 1000, 1.3, true),
    CHASE_NEAR("Cercana", 620, 1.55, true),
    BUMPER("Paragolpes", 330, 0.0, false),
    CHASE_HIGH("Aérea", 1750, 1.45, true);

    private final String label;
    private final double height;
    private final double distanceFactor;
    private final boolean showPlayerCar;

    private ViewMode(String label, double height, double distanceFactor, boolean showPlayerCar) {
        this.label = label;
        this.height = height;
        this.distanceFactor = distanceFactor;
        this.showPlayerCar = showPlayerCar;
    }

    public String label() {
        return label;
    }

    /** Altura de la cámara sobre el piso (unidades de mundo). */
    public double height() {
        return height;
    }

    /** Distancia detrás del auto como fracción de la distancia "clásica" altura*profundidad. */
    public double distanceFactor() {
        return distanceFactor;
    }

    public boolean showPlayerCar() {
        return showPlayerCar;
    }

    public ViewMode next() {
        ViewMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
