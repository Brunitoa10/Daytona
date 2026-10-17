package com.daytona.persistence;

/** Mejor vuelta de una pista, con el fantasma serializado (puede ser null). */
public final class LapRecord {
    private final String trackId;
    private final boolean mirror;
    private final long lapMs;
    private final String playerName;
    private final String carId;
    private final byte[] ghost;

    public LapRecord(String trackId, boolean mirror, long lapMs, String playerName, String carId, byte[] ghost) {
        this.trackId = trackId;
        this.mirror = mirror;
        this.lapMs = lapMs;
        this.playerName = playerName;
        this.carId = carId;
        this.ghost = ghost;
    }

    public String trackId() {
        return trackId;
    }

    public boolean mirror() {
        return mirror;
    }

    public long lapMs() {
        return lapMs;
    }

    public String playerName() {
        return playerName;
    }

    public String carId() {
        return carId;
    }

    public byte[] ghost() {
        return ghost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LapRecord)) {
            return false;
        }
        LapRecord other = (LapRecord) o;
        return java.util.Objects.equals(trackId, other.trackId) && java.util.Objects.equals(mirror, other.mirror) && java.util.Objects.equals(lapMs, other.lapMs) && java.util.Objects.equals(playerName, other.playerName) && java.util.Objects.equals(carId, other.carId) && java.util.Objects.equals(ghost, other.ghost);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(trackId, mirror, lapMs, playerName, carId, ghost);
    }
}
