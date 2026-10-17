package com.daytona.domain.race;

import com.daytona.domain.car.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Participante: auto + conductor + cronometraje. */
public final class Racer {

    private final String name;
    private final Car car;
    private final boolean player;
    private Driver driver;

    int lap = -1;            // -1 = todavía detrás de la línea de largada
    double lapStart;
    double finishTime = -1;
    int position;
    final List<Double> lapTimes = new ArrayList<>();

    public Racer(String name, Car car, Driver driver, boolean player) {
        this.name = name;
        this.car = car;
        this.driver = driver;
        this.player = player;
    }

    public String name() { return name; }
    public Car car() { return car; }
    public Driver driver() { return driver; }
    public boolean isPlayer() { return player; }
    public int lap() { return lap; }
    public double lapStart() { return lapStart; }
    public int position() { return position; }
    public boolean finished() { return finishTime >= 0; }
    public double finishTime() { return finishTime; }
    public List<Double> lapTimes() { return Collections.unmodifiableList(lapTimes); }

    void setDriver(Driver d) {
        this.driver = d;
    }

    /** Distancia total recorrida, usada para calcular posiciones. */
    public double progress(double trackLength) {
        return lap * trackLength + car.z();
    }

    public double bestLap() {
        return lapTimes.stream().mapToDouble(Double::doubleValue).min().orElse(-1);
    }
}
