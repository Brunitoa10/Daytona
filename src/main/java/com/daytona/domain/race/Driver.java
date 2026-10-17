package com.daytona.domain.race;

import com.daytona.domain.car.DriveCommand;

/** Patrón Strategy: quién maneja un auto (jugador, IA, piloto automático). */
public interface Driver {

    public DriveCommand decide(Racer self, RaceContext race, double dt);
}
