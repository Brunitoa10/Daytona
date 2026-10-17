package com.daytona.render;

import com.daytona.domain.car.Car;
import com.daytona.domain.race.GhostData;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.Racer;
import com.daytona.domain.track.Track;
import com.daytona.render.car.CarRenderer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Dibuja una carrera vista desde un auto: fondo, pista, autos, fantasma y partículas. */
public final class RaceView {

    private final RoadRenderer road;
    private final BackgroundRenderer background;
    private final Camera camera = new Camera();
    private final ParticleSystem particles = new ParticleSystem();
    private final List<CarSprite> sprites = new ArrayList<>(24);
    private final double[] ghostOut = new double[2];
    private GhostData ghost;
    private double ghostZ = -1;
    private double emitTimer;

    public RaceView(SpriteAtlas atlas, BackgroundRenderer background, CarRenderer carRenderer) {
        this.road = new RoadRenderer(atlas, carRenderer);
        this.background = background;
    }

    public Camera camera() {
        return camera;
    }

    public void setGhost(GhostData ghost) {
        this.ghost = ghost;
    }

    public double ghostZ() {
        return ghostZ;
    }

    public void update(Race race, Racer focus, double dt) {
        camera.update(dt);
        particles.update(dt);
        emitTimer -= dt;
        if (emitTimer > 0 || !road.playerVisible()) {
            return;
        }
        emitTimer = 1 / 40.0;
        Car car = focus.car();
        Rectangle r = road.playerRect();
        double scale = r.width / 160.0;
        double sp = car.speedRatio();
        if (car.drifting() || (car.braking() && sp > 0.6 && Math.abs(car.steerVisual()) > 0.5)) {
            particles.emit(ParticleSystem.Kind.SMOKE, r.x + r.width * 0.2, r.y + r.height * 0.9, scale, 0xE8E8E8, 1);
            particles.emit(ParticleSystem.Kind.SMOKE, r.x + r.width * 0.8, r.y + r.height * 0.9, scale, 0xE8E8E8, 1);
        }
        if (car.offRoad() && sp > 0.1) {
            int dust = race.track().theme().grassDark();
            particles.emit(ParticleSystem.Kind.DUST, r.x + r.width * 0.5, r.y + r.height * 0.9, scale, dust, 2);
        }
        if (car.scraping() && sp > 0.15) {
            double sx = car.x() < 0 ? r.x : r.x + r.width;
            particles.emit(ParticleSystem.Kind.SPARK, sx, r.y + r.height * 0.75, scale, 0xFFD27A, 4);
        }
    }

    public void render(Graphics2D g, int w, int h, Race race, Racer focus, double alpha, int drawDistance) {
        Track track = race.track();
        camera.follow(track, focus.car(), alpha);
        background.render(g, w, h, track.theme(), camera);

        sprites.clear();
        double len = track.length();
        for (Racer r : race.racers()) {
            if (r == focus && !camera.mode().showPlayerCar()) {
                continue;
            }
            Car c = r.car();
            double dz = c.z() - c.prevZ();
            if (dz < -len / 2) {
                dz += len;
            }
            double z = track.wrap(c.prevZ() + dz * alpha);
            double x = c.prevX() + (c.x() - c.prevX()) * alpha;
            double yaw = c.steerVisual() * 0.55 + c.driftAngle() * 0.5;
            if (r != focus) {
                yaw += (x - focus.car().x()) * -0.25; // perspectiva lateral
            }
            sprites.add(new CarSprite(z, x, c.spec(), yaw, c.braking(), 1f, r == focus));
        }
        ghostZ = -1;
        if (ghost != null && focus.lap() >= 0 && !focus.finished() && ghost.sample(race.currentLapTime(), ghostOut)) {
            ghostZ = track.wrap(ghostOut[0]);
            sprites.add(new CarSprite(ghostZ, ghostOut[1], focus.car().spec(), 0, false, 0.4f, false));
        }
        road.render(g, w, h, track, camera, sprites, drawDistance);
        particles.render(g);
    }

    public void reset() {
        particles.clear();
    }
}
