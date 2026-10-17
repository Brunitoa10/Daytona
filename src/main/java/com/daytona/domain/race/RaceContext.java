package com.daytona.domain.race;

import com.daytona.domain.track.Track;

import java.util.List;

/** Vista de solo lectura de la carrera para los conductores (Interface Segregation). */
public interface RaceContext {

    Track track();

    List<Racer> racers();

    Racer player();

    double elapsed();
}
