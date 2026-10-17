package com.daytona.domain.car;

/** Factory de cajas de cambio. */
public final class Transmissions {

    private Transmissions() {
    }

    public static Transmission create(TransmissionType type, int gears) {
        return type == TransmissionType.MANUAL ? new ManualTransmission(gears) : new AutomaticTransmission(gears);
    }
}
