package com.daytona.domain.race;

import java.util.Arrays;

/** Graba la vuelta en curso del jugador y conserva la más rápida. */
public final class GhostRecorder {

    private static final double INTERVAL = 1.0 / 20;

    private final String carId;
    private float[] t = new float[2048];
    private float[] z = new float[2048];
    private float[] x = new float[2048];
    private int count;
    private double nextSample;
    private GhostData best;

    public GhostRecorder(String carId) {
        this.carId = carId;
    }

    public void startLap() {
        count = 0;
        nextSample = 0;
    }

    /** @param lapTime segundos desde el inicio de la vuelta; zInLap puede ser negativo antes de la línea */
    public void sample(double lapTime, double zInLap, double carX) {
        if (lapTime < nextSample) {
            return;
        }
        nextSample = lapTime + INTERVAL;
        if (count == t.length) {
            t = Arrays.copyOf(t, count * 2);
            z = Arrays.copyOf(z, count * 2);
            x = Arrays.copyOf(x, count * 2);
        }
        t[count] = (float) lapTime;
        z[count] = (float) zInLap;
        x[count] = (float) carX;
        count++;
    }

    public void lapCompleted(double lapTime, double trackLength) {
        sample(lapTime + INTERVAL, trackLength, count > 0 ? x[count - 1] : 0);
        if (best == null || lapTime < best.lapTime()) {
            best = new GhostData(Arrays.copyOf(t, count), Arrays.copyOf(z, count), Arrays.copyOf(x, count), lapTime, carId);
        }
        startLap();
    }

    public GhostData best() {
        return best;
    }
}
