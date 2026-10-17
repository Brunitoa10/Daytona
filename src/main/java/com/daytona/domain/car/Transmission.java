package com.daytona.domain.car;

/** Patrón Strategy: caja automática o manual. */
public interface Transmission {

    public TransmissionType type();

    /** 0 = punto muerto, 1..gearCount. */
    public int gear();

    public int gearCount();

    /**
     * Decide cambios para este paso.
     *
     * @param rpm régimen normalizado 0..1+ en la marcha actual
     * @return true si cambió de marcha
     */
    public boolean update(DriveCommand cmd, double rpm, double speedRatio, double dt);

    public void reset();
}
