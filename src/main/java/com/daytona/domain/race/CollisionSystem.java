package com.daytona.domain.race;

import com.daytona.domain.car.Car;
import com.daytona.domain.track.Segment;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;
import com.daytona.domain.track.TrackSprite;

import java.util.List;

/** Choques auto-auto, auto-escenario y detección de rebufo. */
public final class CollisionSystem {

    public static final double CAR_HALF_WIDTH = 0.15;
    private static final double CAR_LENGTH = 330;
    private static final double SLIPSTREAM_MIN = TrackConstants.SEGMENT_LENGTH * 2;
    private static final double SLIPSTREAM_MAX = TrackConstants.SEGMENT_LENGTH * 11;

    /** Recibe los choques que involucran al jugador. */
    public interface Listener {
        void onPlayerCollision(double intensity);
    }

    public void resolve(Track track, List<Racer> racers, Listener listener) {
        int n = racers.size();
        for (Racer r : racers) {
            r.car().setSlipstream(false);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                carVsCar(track, racers.get(i), racers.get(j), listener);
            }
        }
        for (Racer r : racers) {
            carVsScenery(track, r, listener);
        }
    }

    private static void carVsCar(Track track, Racer a, Racer b, Listener listener) {
        Car ca = a.car();
        Car cb = b.car();
        double len = track.length();
        double ab = track.forwardDistance(ca.z(), cb.z()); // b adelante de a
        Racer rear;
        Racer front;
        double gap;
        if (ab < len / 2) {
            rear = a;
            front = b;
            gap = ab;
        } else {
            rear = b;
            front = a;
            gap = len - ab;
        }
        Car cr = rear.car();
        Car cf = front.car();
        double dx = Math.abs(cr.x() - cf.x());

        if (gap > SLIPSTREAM_MIN && gap < SLIPSTREAM_MAX && dx < 0.22 && cf.speedRatio() > 0.5) {
            cr.setSlipstream(true);
        }
        if (gap >= CAR_LENGTH || dx >= CAR_HALF_WIDTH * 2) {
            return;
        }
        double closing = cr.speed() - cf.speed();
        if (closing > 0) {
            cr.setSpeed(cf.speed() * 0.97);
            cf.setSpeed(cf.speed() + closing * 0.25);
        }
        double push = (CAR_HALF_WIDTH * 2 - dx) * 0.5 + 0.01;
        double side = cr.x() < cf.x() ? -1 : 1;
        cr.setX(cr.x() + side * push);
        cf.setX(cf.x() - side * push * 0.6);
        if ((rear.isPlayer() || front.isPlayer()) && closing > cr.spec().topSpeed() * 0.03) {
            listener.onPlayerCollision(Math.min(1, closing / cr.spec().topSpeed() * 4));
        }
    }

    private static void carVsScenery(Track track, Racer r, Listener listener) {
        Car car = r.car();
        if (Math.abs(car.x()) < 0.95 || car.crashed()) {
            return;
        }
        Segment seg = track.segmentAt(car.z());
        if (seg.walls()) {
            return;
        }
        for (TrackSprite s : seg.sprites()) {
            if (!s.kind().collidable()) {
                continue;
            }
            double w = s.kind().worldWidth() / TrackConstants.ROAD_WIDTH;
            double lo = s.offset() < 0 ? s.offset() - w : s.offset();
            double hi = s.offset() < 0 ? s.offset() : s.offset() + w;
            if (car.x() + CAR_HALF_WIDTH > lo && car.x() - CAR_HALF_WIDTH < hi) {
                double sp = car.speedRatio();
                car.setSpeed(car.speed() * 0.22);
                car.crash(0.6 + sp * 0.6);
                car.setX(s.offset() < 0 ? hi + CAR_HALF_WIDTH + 0.05 : lo - CAR_HALF_WIDTH - 0.05);
                if (r.isPlayer()) {
                    listener.onPlayerCollision(Math.max(0.4, sp));
                }
                return;
            }
        }
    }
}
