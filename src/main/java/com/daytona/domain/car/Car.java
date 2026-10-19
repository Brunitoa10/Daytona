package com.daytona.domain.car;

/**
 * Estado dinámico de un auto en la pista. La lógica que lo modifica vive en
 * {@link CarPhysics} (separación de datos y comportamiento físico).
 */
public final class Car {

    private final CarSpec spec;
    private final Transmission transmission;

    private double z;
    private double x;
    private double speed;
    private double prevZ;
    private double prevX;
    private double rpm;
    private double steerVisual;
    private double driftAngle;
    private boolean drifting;
    private boolean braking;
    private boolean offRoad;
    private boolean scraping;
    private boolean slipstream;
    private double brakeTapTimer;
    private double crashTimer;
    private double throttle;

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
    public double brakeTapTimer() { return brakeTapTimer; }
    public double crashTimer() { return crashTimer; }
    public int gear() { return transmission.gear(); }

    public double speedRatio() {
        return speed / spec.topSpeed();
    }

    // Mutadores usados por sistemas del dominio (colisiones)
    public void setX(double x) { this.x = x; }
    public void setZ(double z) { this.z = z; }
    public void setSpeed(double s) { this.speed = Math.max(0, s); }
    public void setSlipstream(boolean s) { this.slipstream = s; }

    // Mutadores usados por CarPhysics
    public void setRpm(double rpm) { this.rpm = rpm; }
    public void setSteerVisual(double v) { this.steerVisual = v; }
    public void setDriftAngle(double a) { this.driftAngle = a; }
    public void setDrifting(boolean d) { this.drifting = d; }
    public void setBraking(boolean b) { this.braking = b; }
    public void setOffRoad(boolean o) { this.offRoad = o; }
    public void setScraping(boolean s) { this.scraping = s; }
    public void setBrakeTapTimer(double t) { this.brakeTapTimer = t; }
    public void setCrashTimer(double t) { this.crashTimer = t; }
    public void setThrottle(double t) { this.throttle = t; }

    public void crash(double seconds) {
        crashTimer = Math.max(crashTimer, seconds);
    }

    /** Arranque perfecto: impulso inicial. */
    public void boost(double ratio) {
        speed = Math.max(speed, spec.topSpeed() * ratio);
    }
}
