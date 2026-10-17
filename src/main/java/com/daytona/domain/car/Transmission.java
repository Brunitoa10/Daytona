package com.daytona.domain.car;

/** Patrón Strategy: caja automática o manual. */
public interface Transmission {

    TransmissionType type();

    /** 0 = punto muerto, 1..gearCount. */
    int gear();

    int gearCount();

    /**
     * Decide cambios para este paso.
     *
     * @param rpm régimen normalizado 0..1+ en la marcha actual
     * @return true si cambió de marcha
     */
    boolean update(DriveCommand cmd, double rpm, double speedRatio, double dt);

    void reset();
}
