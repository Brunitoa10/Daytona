package com.daytona.persistence;

/** Un resultado de carrera guardado en la tabla de récords. */
public final class RaceResultEntry {
    private final String trackId;
    private final boolean mirror;
    private final String mode;
    private final String carId;
    private final String transmission;
    private final String playerName;
    private final int position;
    private final long totalMs;
    private final long bestLapMs;
    private final long createdAt;

    public RaceResultEntry(String trackId, boolean mirror, String mode, String carId, String transmission, String playerName, int position, long totalMs, long bestLapMs, long createdAt) {
        this.trackId = trackId;
        this.mirror = mirror;
        this.mode = mode;
        this.carId = carId;
        this.transmission = transmission;
        this.playerName = playerName;
        this.position = position;
        this.totalMs = totalMs;
        this.bestLapMs = bestLapMs;
        this.createdAt = createdAt;
    }

    public String trackId() {
        return trackId;
    }

    public boolean mirror() {
        return mirror;
    }

    public String mode() {
        return mode;
    }

    public String carId() {
        return carId;
    }

    public String transmission() {
        return transmission;
    }

    public String playerName() {
        return playerName;
    }

    public int position() {
        return position;
    }

    public long totalMs() {
        return totalMs;
    }

    public long bestLapMs() {
        return bestLapMs;
    }

    public long createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RaceResultEntry)) {
            return false;
        }
        RaceResultEntry other = (RaceResultEntry) o;
        return java.util.Objects.equals(trackId, other.trackId) && java.util.Objects.equals(mirror, other.mirror) && java.util.Objects.equals(mode, other.mode) && java.util.Objects.equals(carId, other.carId) && java.util.Objects.equals(transmission, other.transmission) && java.util.Objects.equals(playerName, other.playerName) && java.util.Objects.equals(position, other.position) && java.util.Objects.equals(totalMs, other.totalMs) && java.util.Objects.equals(bestLapMs, other.bestLapMs) && java.util.Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(trackId, mirror, mode, carId, transmission, playerName, position, totalMs, bestLapMs, createdAt);
    }
}
