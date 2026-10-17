package com.daytona.domain.car;

/**
 * Estado dinámico de un auto en la pista. La lógica que lo modifica vive en
 * {@link CarPhysics} (separación de datos y comportamiento físico).
 */
public final class Car {

    private final CarSpec spec;
    private final Transmission transmission;

    double z;
    double x;
    double speed;
    double prevZ;
    double prevX;
    double rpm;
    double steerVisual;
    double driftAngle;
    boolean drifting;
    boolean braking;
    boolean offRoad;
    boolean scraping;
    boolean slipstream;
    double brakeTapTimer;
    double crashTimer;
    double throttle;

    public Car(CarSpec spec, Transmission transmission) {
        this.spec = spec;
        this.transmission = transmission;
    }

    public void place(double z, double x) {
        this.z = z;
        this.x = x;
        this.prevZ = z;
        this.prevX = x;
        this.speed = 0;
        this.rpm = 0;
        transmission.reset();
    }

    /** Guarda la posición anterior para interpolar el render. */
    public void savePrevious() {
        prevZ = z;
        prevX = x;
    }

    public CarSpec spec() { return spec; }
    public Transmission transmission() { return transmission; }
    public double z() { return z; }
    public double x() { return x; }
    public double speed() { return speed; }
    public double prevZ() { return prevZ; }
    public double prevX() { return prevX; }
    public double rpm() { return rpm; }
    public double steerVisual() { return steerVisual; }
    public double driftAngle() { return driftAngle; }
    public boolean drifting() { return drifting; }
    public boolean braking() { return braking; }
    public boolean offRoad() { return offRoad; }
    public boolean scraping() { return scraping; }
    public boolean slipstream() { return slipstream; }
    public boolean crashed() { return crashTimer > 0; }
    public double throttle() { return throttle; }
    public int gear() { return transmission.gear(); }

    public double speedRatio() {
        return speed / spec.topSpeed();
    }

    // Mutadores usados por sistemas del dominio (colisiones)
    public void setX(double x) { this.x = x; }
    public void setZ(double z) { this.z = z; }
    public void setSpeed(double s) { this.speed = Math.max(0, s); }
    public void setSlipstream(boolean s) { this.slipstream = s; }

    public void crash(double seconds) {
        crashTimer = Math.max(crashTimer, seconds);
    }

    /** Arranque perfecto: impulso inicial. */
    public void boost(double ratio) {
        speed = Math.max(speed, spec.topSpeed() * ratio);
    }
}
