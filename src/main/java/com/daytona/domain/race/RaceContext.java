package com.daytona.domain.race;

import com.daytona.domain.track.Track;

import java.util.List;

/** Vista de solo lectura de la carrera para los conductores (Interface Segregation). */
public interface RaceContext {

    public Track track();

    public List<Racer> racers();

    public Racer player();

    public double elapsed();
}
