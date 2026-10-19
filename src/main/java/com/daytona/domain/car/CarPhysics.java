package com.daytona.domain.car;

import com.daytona.domain.track.Segment;
import com.daytona.domain.track.Track;

/**
 * Física arcade: motor con curva de torque y relaciones de caja, frenos, resistencia,
 * fuerza centrífuga en curvas, derrape (power slide), banquina, muros y rebufo.
 */
public final class CarPhysics {

    /** Límite lateral cuando hay muros. */
    public static final double WALL_LIMIT = 1.12;
    private static final double FREE_LIMIT = 3.2;
    private static final double CENTRIFUGAL = 0.3;
    private static final double OFFROAD_LIMIT = 0.36;

    private static final double[] RATIOS_4 = {0.30, 0.53, 0.77, 1.0};
    private static final double[] RATIOS_6 = {0.20, 0.34, 0.49, 0.64, 0.81, 1.0};

    /** Resultado de un paso, para que la carrera publique eventos. */
    public static final class StepResult {
        private final boolean gearChanged;
        private final boolean startedScraping;
        private final double scrapeIntensity;

        public StepResult(boolean gearChanged, boolean startedScraping, double scrapeIntensity) {
            this.gearChanged = gearChanged;
            this.startedScraping = startedScraping;
            this.scrapeIntensity = scrapeIntensity;
        }

        public boolean gearChanged() {
            return gearChanged;
        }

        public boolean startedScraping() {
            return startedScraping;
        }

        public double scrapeIntensity() {
            return scrapeIntensity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StepResult)) {
                return false;
            }
            StepResult other = (StepResult) o;
            return java.util.Objects.equals(gearChanged, other.gearChanged) && java.util.Objects.equals(startedScraping, other.startedScraping) && java.util.Objects.equals(scrapeIntensity, other.scrapeIntensity);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(gearChanged, startedScraping, scrapeIntensity);
        }
        private static final StepResult NONE = new StepResult(false, false, 0);
    }

    /**
     * Avanza la física un paso.
     *
     * @param moving false durante la cuenta regresiva (solo acelera en vacío)
     */
    public StepResult update(Car car, DriveCommand cmd, Track track, double dt, boolean moving) {
        CarSpec spec = car.spec();
        double top = spec.topSpeed() * (car.slipstream() ? 1.05 : 1.0);
        Transmission tx = car.transmission();
        car.setThrottle(cmd.throttle());
        car.setBraking(cmd.brake() > 0.1);

        if (car.crashTimer() > 0) {
            car.setCrashTimer(car.crashTimer() - dt);
            cmd = new DriveCommand(cmd.steer() * 0.3, 0, 0.4, false, false, cmd.heldGear(), -1);
        }

        double gearTop = gearTopSpeed(spec, tx.gear());
        double rpmInGear = tx.gear() == 0 ? 0 : car.speed() / gearTop;
        boolean shifted = tx.update(cmd, rpmInGear, car.speed() / top, dt);
        gearTop = gearTopSpeed(spec, tx.gear());

        if (!moving) {
            car.setRpm(approach(car.rpm(), 0.15 + 0.85 * cmd.throttle(), dt * 3));
            return new StepResult(shifted, false, 0);
        }

        // ---- longitudinal ----
        double accel = 0;
        if (tx.gear() > 0) {
            double r = car.speed() / gearTop;
            car.setRpm(Math.max(0.12, Math.min(1.05, r)));
            if (r < 1.0) {
                accel = cmd.throttle() * baseAccel(spec) * torque(r) * gearMultiplier(tx.gear(), tx.gearCount());
            }
            if (r > 1.02) {
                accel -= spec.topSpeed() * 0.25; // freno motor por sobre-régimen
            }
            accel -= (1 - cmd.throttle()) * spec.topSpeed() * 0.04; // freno motor
        } else {
            car.setRpm(approach(car.rpm(), 0.12 + 0.9 * cmd.throttle(), dt * 4));
        }
        if (car.slipstream()) {
            accel *= 1.08;
        }
        double ratio = car.speed() / spec.topSpeed();
        double drag = spec.topSpeed() * (0.02 + 0.066 * ratio * ratio);
        double brake = cmd.brake() * spec.topSpeed() * 0.55 * spec.braking();
        car.setSpeed(car.speed() + (accel - drag - brake) * dt);

        Segment seg = track.segmentAt(car.z());
        double limit = seg.walls() ? WALL_LIMIT : FREE_LIMIT;
        car.setOffRoad(Math.abs(car.x()) > 1.0 && !seg.walls());
        if (car.offRoad() && car.speed() > spec.topSpeed() * OFFROAD_LIMIT) {
            car.setSpeed(car.speed() - spec.topSpeed() * 0.9 * dt);
        }
        if (car.drifting()) {
            car.setSpeed(car.speed() - spec.topSpeed() * 0.07 * dt);
        }
        car.setSpeed(Math.max(0, Math.min(car.speed(), top * 1.02)));

        // ---- lateral ----
        double sp = car.speed() / spec.topSpeed();
        updateDrift(car, cmd, sp, dt);
        double dx = dt * 2.0 * sp;
        double authority = spec.handling() * (car.drifting() ? 1.35 : 1.0);
        car.setX(car.x() + cmd.steer() * dx * authority);
        car.setX(car.x() - dx * sp * seg.curve() * CENTRIFUGAL / spec.grip() * (car.drifting() ? 0.72 : 1.0));

        boolean wasScraping = car.scraping();
        car.setScraping(false);
        double intensity = 0;
        if (Math.abs(car.x()) > limit) {
            car.setX(Math.copySign(limit, car.x()));
            if (seg.walls()) {
                car.setScraping(true);
                car.setSpeed(car.speed() - spec.topSpeed() * 0.28 * dt);
                intensity = sp;
            }
        }

        car.setSteerVisual(approach(car.steerVisual(), cmd.steer(), dt * 6));
        car.setDriftAngle(approach(car.driftAngle(), car.drifting() ? Math.signum(cmd.steer()) : 0, dt * 4));

        // ---- avance ----
        car.setZ(track.wrap(car.z() + car.speed() * dt));
        return shifted || (car.scraping() && !wasScraping)
                ? new StepResult(shifted, car.scraping() && !wasScraping, intensity)
                : StepResult.NONE;
    }

    /** Derrape: golpe de freno + volantazo a alta velocidad; se mantiene mientras se dobla. */
    private static void updateDrift(Car car, DriveCommand cmd, double sp, double dt) {
        if (cmd.brake() > 0.3) {
            car.setBrakeTapTimer(0.4);
        } else {
            car.setBrakeTapTimer(Math.max(0, car.brakeTapTimer() - dt));
        }
        if (!car.drifting()) {
            if (Math.abs(cmd.steer()) > 0.6 && sp > 0.5 && car.brakeTapTimer() > 0 && cmd.brake() < 0.3 && cmd.throttle() > 0.4) {
                car.setDrifting(true);
            }
        } else if (Math.abs(cmd.steer()) < 0.3 || sp < 0.35 || car.offRoad()) {
            car.setDrifting(false);
        }
    }

    public static double gearTopSpeed(CarSpec spec, int gear) {
        double[] ratios = spec.gears() >= 6 ? RATIOS_6 : RATIOS_4;
        int g = Math.max(1, Math.min(gear, ratios.length));
        return spec.topSpeed() * ratios[g - 1] * 1.02;
    }

    private static double baseAccel(CarSpec spec) {
        return spec.topSpeed() / 5.2 * spec.accel();
    }

    /** Curva de torque: baja en bajo régimen, máxima a ~75 %, cae al corte. */
    private static double torque(double r) {
        if (r < 0.25) {
            return 0.55 + r * 1.2;
        }
        if (r < 0.78) {
            return 0.85 + (r - 0.25) * 0.28;
        }
        return Math.max(0.6, 1.0 - (r - 0.78) * 1.1);
    }

    private static double gearMultiplier(int gear, int count) {
        if (count <= 1) {
            return 1;
        }
        return 1.5 - 0.78 * (gear - 1) / (double) (count - 1);
    }

    private static double approach(double v, double target, double step) {
        return v < target ? Math.min(target, v + step) : Math.max(target, v - step);
    }
}
