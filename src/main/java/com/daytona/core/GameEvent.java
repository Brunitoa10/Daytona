package com.daytona.core;

/**
 * Eventos que ocurren durante una carrera. Son inmutables.
 */
public interface GameEvent {

    public static final class CountdownTick implements GameEvent {
        private final int value;

        public CountdownTick(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CountdownTick)) {
                return false;
            }
            CountdownTick other = (CountdownTick) o;
            return java.util.Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(value);
        }
    }

    public static final class RaceStarted implements GameEvent {
        private final boolean perfectStart;

        public RaceStarted(boolean perfectStart) {
            this.perfectStart = perfectStart;
        }

        public boolean perfectStart() {
            return perfectStart;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RaceStarted)) {
                return false;
            }
            RaceStarted other = (RaceStarted) o;
            return java.util.Objects.equals(perfectStart, other.perfectStart);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(perfectStart);
        }
    }

    public static final class LapCompleted implements GameEvent {
        private final boolean player;
        private final int lap;
        private final double lapTime;
        private final boolean bestLap;
        private final boolean finalLapNext;

        public LapCompleted(boolean player, int lap, double lapTime, boolean bestLap, boolean finalLapNext) {
            this.player = player;
            this.lap = lap;
            this.lapTime = lapTime;
            this.bestLap = bestLap;
            this.finalLapNext = finalLapNext;
        }

        public boolean player() {
            return player;
        }

        public int lap() {
            return lap;
        }

        public double lapTime() {
            return lapTime;
        }

        public boolean bestLap() {
            return bestLap;
        }

        public boolean finalLapNext() {
            return finalLapNext;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LapCompleted)) {
                return false;
            }
            LapCompleted other = (LapCompleted) o;
            return java.util.Objects.equals(player, other.player) && java.util.Objects.equals(lap, other.lap) && java.util.Objects.equals(lapTime, other.lapTime) && java.util.Objects.equals(bestLap, other.bestLap) && java.util.Objects.equals(finalLapNext, other.finalLapNext);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(player, lap, lapTime, bestLap, finalLapNext);
        }
    }

    public static final class CheckpointPassed implements GameEvent {
        private final double bonusSeconds;

        public CheckpointPassed(double bonusSeconds) {
            this.bonusSeconds = bonusSeconds;
        }

        public double bonusSeconds() {
            return bonusSeconds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CheckpointPassed)) {
                return false;
            }
            CheckpointPassed other = (CheckpointPassed) o;
            return java.util.Objects.equals(bonusSeconds, other.bonusSeconds);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(bonusSeconds);
        }
    }

    public static final class Collision implements GameEvent {
        private final boolean player;
        private final double intensity;

        public Collision(boolean player, double intensity) {
            this.player = player;
            this.intensity = intensity;
        }

        public boolean player() {
            return player;
        }

        public double intensity() {
            return intensity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Collision)) {
                return false;
            }
            Collision other = (Collision) o;
            return java.util.Objects.equals(player, other.player) && java.util.Objects.equals(intensity, other.intensity);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(player, intensity);
        }
    }

    public static final class WallScrape implements GameEvent {
        private final boolean player;
        private final double intensity;

        public WallScrape(boolean player, double intensity) {
            this.player = player;
            this.intensity = intensity;
        }

        public boolean player() {
            return player;
        }

        public double intensity() {
            return intensity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WallScrape)) {
                return false;
            }
            WallScrape other = (WallScrape) o;
            return java.util.Objects.equals(player, other.player) && java.util.Objects.equals(intensity, other.intensity);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(player, intensity);
        }
    }

    public static final class GearChanged implements GameEvent {
        private final int gear;

        public GearChanged(int gear) {
            this.gear = gear;
        }

        public int gear() {
            return gear;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GearChanged)) {
                return false;
            }
            GearChanged other = (GearChanged) o;
            return java.util.Objects.equals(gear, other.gear);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(gear);
        }
    }

    public static final class TimeOver implements GameEvent {

        public TimeOver() {
        }
    }

    public static final class RaceFinished implements GameEvent {
        private final int playerPosition;

        public RaceFinished(int playerPosition) {
            this.playerPosition = playerPosition;
        }

        public int playerPosition() {
            return playerPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RaceFinished)) {
                return false;
            }
            RaceFinished other = (RaceFinished) o;
            return java.util.Objects.equals(playerPosition, other.playerPosition);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(playerPosition);
        }
    }
}
