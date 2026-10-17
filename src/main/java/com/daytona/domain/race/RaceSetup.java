package com.daytona.domain.race;

import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.car.CarSpec;
import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.track.Track;

/**
 * Todo lo necesario para armar una carrera.
 *
 * @param playerDriver conductor del jugador (null = modo demostración, todos IA)
 * @param ghost        fantasma a mostrar (puede ser null)
 */
public final class RaceSetup {
    private final Track track;
    private final CarSpec playerCar;
    private final TransmissionType transmission;
    private final GameMode mode;
    private final int laps;
    private final int rivals;
    private final Difficulty difficulty;
    private final String playerName;
    private final Driver playerDriver;
    private final GhostData ghost;
    private final CarCatalog catalog;
    private final long seed;

    public RaceSetup(Track track, CarSpec playerCar, TransmissionType transmission, GameMode mode, int laps, int rivals, Difficulty difficulty, String playerName, Driver playerDriver, GhostData ghost, CarCatalog catalog, long seed) {
        this.track = track;
        this.playerCar = playerCar;
        this.transmission = transmission;
        this.mode = mode;
        this.laps = laps;
        this.rivals = rivals;
        this.difficulty = difficulty;
        this.playerName = playerName;
        this.playerDriver = playerDriver;
        this.ghost = ghost;
        this.catalog = catalog;
        this.seed = seed;
    }

    public Track track() {
        return track;
    }

    public CarSpec playerCar() {
        return playerCar;
    }

    public TransmissionType transmission() {
        return transmission;
    }

    public GameMode mode() {
        return mode;
    }

    public int laps() {
        return laps;
    }

    public int rivals() {
        return rivals;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public String playerName() {
        return playerName;
    }

    public Driver playerDriver() {
        return playerDriver;
    }

    public GhostData ghost() {
        return ghost;
    }

    public CarCatalog catalog() {
        return catalog;
    }

    public long seed() {
        return seed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RaceSetup)) {
            return false;
        }
        RaceSetup other = (RaceSetup) o;
        return java.util.Objects.equals(track, other.track) && java.util.Objects.equals(playerCar, other.playerCar) && java.util.Objects.equals(transmission, other.transmission) && java.util.Objects.equals(mode, other.mode) && java.util.Objects.equals(laps, other.laps) && java.util.Objects.equals(rivals, other.rivals) && java.util.Objects.equals(difficulty, other.difficulty) && java.util.Objects.equals(playerName, other.playerName) && java.util.Objects.equals(playerDriver, other.playerDriver) && java.util.Objects.equals(ghost, other.ghost) && java.util.Objects.equals(catalog, other.catalog) && java.util.Objects.equals(seed, other.seed);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(track, playerCar, transmission, mode, laps, rivals, difficulty, playerName, playerDriver, ghost, catalog, seed);
    }
}
