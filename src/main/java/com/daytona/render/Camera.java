package com.daytona.render;

import com.daytona.domain.car.Car;
import com.daytona.domain.track.Track;

import java.util.Random;

/** Cámara de persecución con interpolación, sacudón por choque y paralaje del cielo. */
public final class Camera {

    public static final double FIELD_OF_VIEW = 100;
    public static final double DEPTH = 1 / Math.tan(Math.toRadians(FIELD_OF_VIEW / 2));

    private final Random random = new Random();
    private ViewMode mode = ViewMode.CHASE_FAR;
    private double x;
    private double y;
    private double z;
    private double shake;
    private double shakeX;
    private double shakeY;
    private double skyOffset;
    private double hillOffset;
    private double nearOffset;
    private double lastFollowZ = -1;

    public void setMode(ViewMode mode) {
        this.mode = mode;
    }

    public ViewMode mode() {
        return mode;
    }

    /** Distancia de la cámara detrás del auto. */
    public double followDistance() {
        return mode.height() * DEPTH * mode.distanceFactor() + (mode == ViewMode.BUMPER ? -60 : 0);
    }

    /**
     * Ubica la cámara detrás del auto, usando la posición interpolada.
     */
    public void follow(Track track, Car car, double alpha) {
        double len = track.length();
        double dz = car.z() - car.prevZ();
        if (dz < -len / 2) {
            dz += len;
        }
        double carZ = track.wrap(car.prevZ() + dz * alpha);
        double carX = car.prevX() + (car.x() - car.prevX()) * alpha;
        double dist = followDistance();
        z = track.wrap(carZ - dist);
        double camX = track.segmentAt(carZ).walls() ? Math.max(-0.95, Math.min(0.95, carX)) : carX;
        x = camX * com.daytona.domain.track.TrackConstants.ROAD_WIDTH; // con muros, la cámara no los atraviesa
        y = track.heightAt(z) + mode.height();
        if (mode == ViewMode.BUMPER) {
            y = Math.max(y, track.heightAt(carZ) + mode.height());
        }
        if (lastFollowZ >= 0) {
            double moved = carZ - lastFollowZ;
            if (moved < -len / 2) {
                moved += len;
            }
            double curve = track.segmentAt(carZ).curve();
            double seg = com.daytona.domain.track.TrackConstants.SEGMENT_LENGTH;
            skyOffset = wrap01(skyOffset + 0.001 * curve * moved / seg);
            hillOffset = wrap01(hillOffset + 0.002 * curve * moved / seg);
            nearOffset = wrap01(nearOffset + 0.003 * curve * moved / seg);
        }
        lastFollowZ = carZ;
    }

    public void addShake(double amount) {
        shake = Math.min(1, shake + amount);
    }

    public void update(double dt) {
        shake = Math.max(0, shake - dt * 2.5);
        shakeX = (random.nextDouble() - 0.5) * shake * 14;
        shakeY = (random.nextDouble() - 0.5) * shake * 10;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public double shakeX() { return shakeX; }
    public double shakeY() { return shakeY; }
    public double skyOffset() { return skyOffset; }
    public double hillOffset() { return hillOffset; }
    public double nearOffset() { return nearOffset; }

    private static double wrap01(double v) {
        return v - Math.floor(v);
    }
}
