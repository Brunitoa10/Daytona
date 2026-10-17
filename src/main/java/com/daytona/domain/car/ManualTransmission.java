package com.daytona.domain.car;

/** Caja manual: secuencial (subir/bajar), selección directa o palanca H con punto muerto. */
public final class ManualTransmission implements Transmission {

    private final int gearCount;
    private int gear = 1;

    public ManualTransmission(int gearCount) {
        this.gearCount = gearCount;
    }

    @Override
    public TransmissionType type() {
        return TransmissionType.MANUAL;
    }

    @Override
    public int gear() {
        return gear;
    }

    @Override
    public int gearCount() {
        return gearCount;
    }

    @Override
    public boolean update(DriveCommand cmd, double rpm, double speedRatio, double dt) {
        int before = gear;
        if (cmd.heldGear() >= 0) {
            gear = Math.min(cmd.heldGear(), gearCount);
        } else if (cmd.requestedGear() > 0) {
            gear = Math.min(cmd.requestedGear(), gearCount);
        } else if (cmd.shiftUp() && gear < gearCount) {
            gear++;
        } else if (cmd.shiftDown() && gear > 1) {
            gear--;
        }
        return gear != before;
    }

    @Override
    public void reset() {
        gear = 1;
    }
}
