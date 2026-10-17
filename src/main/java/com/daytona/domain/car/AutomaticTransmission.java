package com.daytona.domain.car;

public final class AutomaticTransmission implements Transmission {

    private static final double UP_RPM = 0.93;
    private static final double DOWN_RPM = 0.48;

    private final int gearCount;
    private int gear = 1;
    private double cooldown;

    public AutomaticTransmission(int gearCount) {
        this.gearCount = gearCount;
    }

    @Override
    public TransmissionType type() {
        return TransmissionType.AUTOMATIC;
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
        if (cooldown > 0) {
            cooldown -= dt;
            return false;
        }
        if (rpm > UP_RPM && gear < gearCount && cmd.throttle() > 0.1) {
            gear++;
            cooldown = 0.35;
            return true;
        }
        if (rpm < DOWN_RPM && gear > 1) {
            gear--;
            cooldown = 0.25;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        gear = 1;
        cooldown = 0;
    }
}
