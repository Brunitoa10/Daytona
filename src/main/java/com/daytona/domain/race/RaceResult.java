package com.daytona.domain.race;

/** Fila de la tabla final. totalTime es estimado si no terminó. */
public final class RaceResult {
    private final String name;
    private final boolean player;
    private final int position;
    private final double totalTime;
    private final double bestLap;
    private final boolean finished;
    private final String carName;
    private final int number;
    private final int bodyRgb;

    public RaceResult(String name, boolean player, int position, double totalTime, double bestLap, boolean finished, String carName, int number, int bodyRgb) {
        this.name = name;
        this.player = player;
        this.position = position;
        this.totalTime = totalTime;
        this.bestLap = bestLap;
        this.finished = finished;
        this.carName = carName;
        this.number = number;
        this.bodyRgb = bodyRgb;
    }

    public String name() {
        return name;
    }

    public boolean player() {
        return player;
    }

    public int position() {
        return position;
    }

    public double totalTime() {
        return totalTime;
    }

    public double bestLap() {
        return bestLap;
    }

    public boolean finished() {
        return finished;
    }

    public String carName() {
        return carName;
    }

    public int number() {
        return number;
    }

    public int bodyRgb() {
        return bodyRgb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RaceResult)) {
            return false;
        }
        RaceResult other = (RaceResult) o;
        return java.util.Objects.equals(name, other.name) && java.util.Objects.equals(player, other.player) && java.util.Objects.equals(position, other.position) && java.util.Objects.equals(totalTime, other.totalTime) && java.util.Objects.equals(bestLap, other.bestLap) && java.util.Objects.equals(finished, other.finished) && java.util.Objects.equals(carName, other.carName) && java.util.Objects.equals(number, other.number) && java.util.Objects.equals(bodyRgb, other.bodyRgb);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, player, position, totalTime, bestLap, finished, carName, number, bodyRgb);
    }
}
