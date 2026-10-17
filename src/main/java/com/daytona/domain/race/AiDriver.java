package com.daytona.domain.race;

import com.daytona.domain.car.Car;
import com.daytona.domain.car.DriveCommand;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;

import java.util.Random;

/**
 * IA de rivales: elige carril, toma el interior de las curvas, frena antes de las
 * cerradas, esquiva autos más lentos y aplica "rubber band" leve según el modo.
 */
public final class AiDriver implements Driver {

    private static final double LOOK_AHEAD_CARS = 1600;

    private final double skill;
    private final double rubberBand;
    private final Random random;
    private double lane;
    private double laneTimer;

    public AiDriver(double skill, double rubberBand, long seed) {
        this.skill = skill;
        this.rubberBand = rubberBand;
        this.random = new Random(seed);
        this.lane = -0.6 + random.nextDouble() * 1.2;
    }

    @Override
    public DriveCommand decide(Racer self, RaceContext race, double dt) {
        Car car = self.car();
        Track track = race.track();
        double top = car.spec().topSpeed();
        double sp = car.speed() / top;

        laneTimer -= dt;
        if (laneTimer <= 0) {
            laneTimer = 3 + random.nextDouble() * 5;
            lane = Math.max(-0.7, Math.min(0.7, lane + (random.nextDouble() - 0.5) * 0.6));
        }

        double curveNear = track.segmentAt(car.z()).curve();
        double curveAhead = track.curvatureAhead(car.z(), 30);
        double target = lane - Math.signum(curveAhead) * Math.min(0.45, Math.abs(curveAhead) / 120);

        Car blocker = carAhead(self, race);
        if (blocker != null && blocker.speed() < car.speed() * 1.02) {
            double left = blocker.x() - 0.5;
            double right = blocker.x() + 0.5;
            target = Math.abs(left - car.x()) < Math.abs(right - car.x()) && left > -0.8 ? left : Math.min(0.8, right);
        }

        double feedForward = sp * curveNear * 0.3 / (car.spec().grip() * car.spec().handling());
        double steer = (target - car.x()) * 3.5 + feedForward;
        steer = Math.max(-1, Math.min(1, steer));

        double maxCurve = track.maxCurveAhead(car.z(), 8 + (int) (sp * 30));
        double desired = top * Math.min(skill * rubber(self, race), cornerSpeed(maxCurve, car));
        double throttle = car.speed() < desired ? 1 : 0.2;
        double brake = car.speed() > desired * 1.15 ? 1.0 : car.speed() > desired * 1.05 ? 0.5 : 0;
        if (Math.abs(car.x()) > 1.0) {
            throttle = 0.7;
        }
        return DriveCommand.of(steer, throttle, brake);
    }

    private double rubber(Racer self, RaceContext race) {
        if (rubberBand <= 0 || race.player() == null || race.player() == self) {
            return 1;
        }
        double len = race.track().length();
        double gap = (race.player().progress(len) - self.progress(len)) / len; // >0 = el jugador va adelante
        return 1 + Math.max(-0.06, Math.min(0.06, gap * rubberBand));
    }

    /** Velocidad (fracción de la punta) a la que la dirección todavía vence a la centrífuga. */
    private static double cornerSpeed(double curve, Car car) {
        if (curve < 0.5) {
            return 1.2;
        }
        return 0.76 * car.spec().handling() * car.spec().grip() / (0.3 * curve);
    }

    private static Car carAhead(Racer self, RaceContext race) {
        Car me = self.car();
        Car best = null;
        double bestDist = LOOK_AHEAD_CARS;
        for (Racer r : race.racers()) {
            if (r == self) {
                continue;
            }
            double d = race.track().forwardDistance(me.z(), r.car().z());
            if (d < bestDist && Math.abs(r.car().x() - me.x()) < 0.42 && d > TrackConstants.SEGMENT_LENGTH * 0.2) {
                best = r.car();
                bestDist = d;
            }
        }
        return best;
    }
}
