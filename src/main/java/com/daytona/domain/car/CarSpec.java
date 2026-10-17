package com.daytona.domain.car;

import com.daytona.domain.track.TrackConstants;

/**
 * Ficha técnica inmutable de un auto.
 *
 * @param accel    multiplicador de aceleración (1 = referencia)
 * @param grip     agarre en curva (más alto = menos fuerza centrífuga)
 * @param handling respuesta de la dirección
 * @param braking  potencia de frenado
 */
public final class CarSpec {
    private final String id;
    private final String name;
    private final String tagline;
    private final int bodyRgb;
    private final int stripeRgb;
    private final double topSpeedKmh;
    private final double accel;
    private final double grip;
    private final double handling;
    private final double braking;
    private final int gears;
    private final int number;

    public CarSpec(String id, String name, String tagline, int bodyRgb, int stripeRgb, double topSpeedKmh, double accel, double grip, double handling, double braking, int gears, int number) {
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.bodyRgb = bodyRgb;
        this.stripeRgb = stripeRgb;
        this.topSpeedKmh = topSpeedKmh;
        this.accel = accel;
        this.grip = grip;
        this.handling = handling;
        this.braking = braking;
        this.gears = gears;
        this.number = number;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String tagline() {
        return tagline;
    }

    public int bodyRgb() {
        return bodyRgb;
    }

    public int stripeRgb() {
        return stripeRgb;
    }

    public double topSpeedKmh() {
        return topSpeedKmh;
    }

    public double accel() {
        return accel;
    }

    public double grip() {
        return grip;
    }

    public double handling() {
        return handling;
    }

    public double braking() {
        return braking;
    }

    public int gears() {
        return gears;
    }

    public int number() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarSpec)) {
            return false;
        }
        CarSpec other = (CarSpec) o;
        return java.util.Objects.equals(id, other.id) && java.util.Objects.equals(name, other.name) && java.util.Objects.equals(tagline, other.tagline) && java.util.Objects.equals(bodyRgb, other.bodyRgb) && java.util.Objects.equals(stripeRgb, other.stripeRgb) && java.util.Objects.equals(topSpeedKmh, other.topSpeedKmh) && java.util.Objects.equals(accel, other.accel) && java.util.Objects.equals(grip, other.grip) && java.util.Objects.equals(handling, other.handling) && java.util.Objects.equals(braking, other.braking) && java.util.Objects.equals(gears, other.gears) && java.util.Objects.equals(number, other.number);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, tagline, bodyRgb, stripeRgb, topSpeedKmh, accel, grip, handling, braking, gears, number);
    }
    public double topSpeed() {
        return TrackConstants.kmhToUnits(topSpeedKmh);
    }

    /** Variante con otro color y número (para los rivales). */
    public CarSpec withLivery(int body, int stripe, int newNumber) {
        return new CarSpec(id, name, tagline, body, stripe, topSpeedKmh, accel, grip, handling, braking, gears, newNumber);
    }

    /** Variante con prestaciones escaladas (habilidad de la IA). */
    public CarSpec scaled(double factor) {
        return new CarSpec(id, name, tagline, bodyRgb, stripeRgb, topSpeedKmh * factor, accel * factor,
                grip, handling, braking, gears, number);
    }

    /** Puntuación 1..10 para las barras de la pantalla de selección. */
    public int rating(double value, double min, double max) {
        return (int) Math.round(1 + 9 * Math.max(0, Math.min(1, (value - min) / (max - min))));
    }
}
