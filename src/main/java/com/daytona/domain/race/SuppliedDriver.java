package com.daytona.domain.race;

import com.daytona.domain.car.DriveCommand;

import java.util.function.DoubleFunction;

/** Conductor humano: toma el comando de la fuente de entrada activa (recibe dt). */
public final class SuppliedDriver implements Driver {

    private final DoubleFunction<DriveCommand> source;

    public SuppliedDriver(DoubleFunction<DriveCommand> source) {
        this.source = source;
    }

    @Override
    public DriveCommand decide(Racer self, RaceContext race, double dt) {
        return source.apply(dt);
    }
}
