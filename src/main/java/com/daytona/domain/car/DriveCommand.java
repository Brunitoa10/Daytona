package com.daytona.domain.car;

/**
 * Lo que el conductor (humano o IA) quiere hacer en este paso. Inmutable.
 *
 * @param steer         -1 (izquierda) .. 1 (derecha)
 * @param throttle      0..1
 * @param brake         0..1
 * @param shiftUp       flanco: pidió subir marcha
 * @param shiftDown     flanco: pidió bajar marcha
 * @param heldGear      palanca H: -1 = no se usa, 0 = punto muerto, 1..6 = marcha enganchada
 * @param requestedGear selección directa por flanco (teclas 1-6): -1 = ninguna
 */
public final class DriveCommand {
    private final double steer;
    private final double throttle;
    private final double brake;
    private final boolean shiftUp;
    private final boolean shiftDown;
    private final int heldGear;
    private final int requestedGear;

    public DriveCommand(double steer, double throttle, double brake, boolean shiftUp, boolean shiftDown, int heldGear, int requestedGear) {
        this.steer = steer;
        this.throttle = throttle;
        this.brake = brake;
        this.shiftUp = shiftUp;
        this.shiftDown = shiftDown;
        this.heldGear = heldGear;
        this.requestedGear = requestedGear;
    }

    public double steer() {
        return steer;
    }

    public double throttle() {
        return throttle;
    }

    public double brake() {
        return brake;
    }

    public boolean shiftUp() {
        return shiftUp;
    }

    public boolean shiftDown() {
        return shiftDown;
    }

    public int heldGear() {
        return heldGear;
    }

    public int requestedGear() {
        return requestedGear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DriveCommand)) {
            return false;
        }
        DriveCommand other = (DriveCommand) o;
        return java.util.Objects.equals(steer, other.steer) && java.util.Objects.equals(throttle, other.throttle) && java.util.Objects.equals(brake, other.brake) && java.util.Objects.equals(shiftUp, other.shiftUp) && java.util.Objects.equals(shiftDown, other.shiftDown) && java.util.Objects.equals(heldGear, other.heldGear) && java.util.Objects.equals(requestedGear, other.requestedGear);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(steer, throttle, brake, shiftUp, shiftDown, heldGear, requestedGear);
    }
    public static final DriveCommand IDLE = new DriveCommand(0, 0, 0, false, false, -1, -1);

    public static DriveCommand of(double steer, double throttle, double brake) {
        return new DriveCommand(steer, throttle, brake, false, false, -1, -1);
    }

    public DriveCommand coasting() {
        return new DriveCommand(steer, 0, 0.25, false, false, -1, -1);
    }
}
