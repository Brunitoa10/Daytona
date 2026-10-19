package com.daytona.domain.race;

import com.daytona.core.EventBus;
import com.daytona.core.GameEvent;
import com.daytona.domain.car.Car;
import com.daytona.domain.car.CarPhysics;
import com.daytona.domain.car.CarSpec;
import com.daytona.domain.car.DriveCommand;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Orquesta una carrera: largada, física de todos los autos, choques, vueltas,
 * checkpoints, reloj, posiciones, fantasma y final. No sabe nada de pantallas ni
 * de dispositivos: recibe conductores y publica eventos (bajo acoplamiento).
 */
public final class Race implements RaceContext {

    public enum Phase { COUNTDOWN, RACING, FINISHED, TIME_OVER }

    public static final double COUNTDOWN_SECONDS = 3.0;
    private static final double END_DELAY = 4.5;

    private final RaceSetup setup;
    private final Track track;
    private final EventBus bus;
    private final CarPhysics physics = new CarPhysics();
    private final CollisionSystem collisions = new CollisionSystem();
    private final List<Racer> racers = new ArrayList<>();
    private final Racer player;
    private final List<Racer> readOnlyRacers = Collections.unmodifiableList(racers);
    private final Standings standings;
    private final GhostRecorder ghostRecorder;

    private Phase phase = Phase.COUNTDOWN;
    private double countdown = COUNTDOWN_SECONDS;
    private int lastTick = 4;
    private double elapsed;
    private double timeLeft;
    private int playerSection;
    private double endTimer;
    private double bestLapOverall = -1;

    public Race(RaceSetup setup, EventBus bus) {
        this.setup = setup;
        this.track = setup.track();
        this.bus = bus;
        this.ghostRecorder = new GhostRecorder(setup.playerCar().id());
        this.timeLeft = setup.mode().timeLimited() ? setup.mode().initialTime(track, setup.difficulty()) : 0;
        GridBuilder.Grid grid = GridBuilder.build(setup);
        this.racers.addAll(grid.racers());
        this.player = grid.player();
        this.standings = new Standings(racers, track.length());
    }

    // ------------------------------------------------------------------ ciclo

    public void update(double dt) {
        for (Racer r : racers) {
            r.car().savePrevious();
        }
        if (phase == Phase.COUNTDOWN) {
            updateCountdown(dt);
            return;
        }
        elapsed += dt;
        for (Racer r : racers) {
            DriveCommand cmd = r.driver().decide(r, this, dt);
            if (r == player && phase == Phase.TIME_OVER) {
                cmd = cmd.coasting();
            }
            CarPhysics.StepResult res = physics.update(r.car(), cmd, track, dt, true);
            if (r == player && r.isPlayer()) {
                if (res.gearChanged()) {
                    bus.publish(new GameEvent.GearChanged(r.car().gear()));
                }
                if (res.startedScraping()) {
                    bus.publish(new GameEvent.WallScrape(true, res.scrapeIntensity()));
                }
            }
        }
        collisions.resolve(track, racers, intensity -> {
            if (player.isPlayer()) {
                bus.publish(new GameEvent.Collision(true, intensity));
            }
        });
        for (Racer r : racers) {
            trackProgress(r);
        }
        if (player.lap() >= 0 && !player.finished()) {
            ghostRecorder.sample(elapsed - player.lapStart(), player.car().z(), player.car().x());
        }
        updatePositions();
        updateClock(dt);
    }

    private void updateCountdown(double dt) {
        countdown -= dt;
        int tick = (int) Math.ceil(countdown);
        if (tick != lastTick && tick > 0) {
            lastTick = tick;
            bus.publish(new GameEvent.CountdownTick(tick));
        }
        for (Racer r : racers) {
            physics.update(r.car(), r.driver().decide(r, this, dt), track, dt, false);
        }
        if (countdown <= 0) {
            phase = Phase.RACING;
            bus.publish(new GameEvent.CountdownTick(0));
            double rpm = player.car().rpm();
            boolean perfect = player.isPlayer() && rpm > 0.55 && rpm < 0.85;
            if (perfect) {
                player.car().boost(0.16);
            }
            Random rnd = new Random(setup.seed() + 7);
            for (Racer r : racers) {
                if (r != player) {
                    r.car().boost(0.04 + rnd.nextDouble() * 0.08);
                }
            }
            bus.publish(new GameEvent.RaceStarted(perfect));
        }
    }

    private void updateClock(double dt) {
        if (phase == Phase.RACING && setup.mode().timeLimited() && player.isPlayer()) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                timeLeft = 0;
                phase = Phase.TIME_OVER;
                bus.publish(new GameEvent.TimeOver());
            }
        }
        if (phase == Phase.FINISHED || phase == Phase.TIME_OVER) {
            endTimer += dt;
        }
    }

    private void trackProgress(Racer r) {
        Car car = r.car();
        double len = track.length();
        double prev = car.prevZ();
        double now = car.z();
        boolean crossedLine = prev > len * 0.6 && now < len * 0.4;
        if (r == player && r.lap() >= 0 && !crossedLine) {
            checkCheckpoints(prev, now);
        }
        if (crossedLine) {
            onLineCrossed(r);
        }
    }

    private void checkCheckpoints(double prev, double now) {
        int[] cps = track.checkpoints();
        if (playerSection >= cps.length) {
            return;
        }
        double cpZ = cps[playerSection] * TrackConstants.SEGMENT_LENGTH;
        if (prev < cpZ && now >= cpZ) {
            if (setup.mode().timeLimited() && phase == Phase.RACING) {
                double bonus = setup.mode().sectionBonus(track, setup.difficulty(), playerSection);
                timeLeft += bonus;
                bus.publish(new GameEvent.CheckpointPassed(bonus));
            }
            playerSection++;
        }
    }

    private void onLineCrossed(Racer r) {
        r.incrementLap();
        if (r.lap() == 0) {
            if (r == player) {
                ghostRecorder.startLap();
            }
            return;
        }
        if (r.finished()) {
            return;
        }
        double lapTime = elapsed - r.lapStart();
        r.addLapTime(lapTime);
        r.setLapStart(elapsed);
        boolean best = bestLapOverall < 0 || lapTime < bestLapOverall;
        if (best) {
            bestLapOverall = lapTime;
        }
        boolean finishedNow = r.lap() >= setup.laps();
        if (finishedNow) {
            r.setFinishTime(elapsed);
        }
        if (r != player) {
            return;
        }
        ghostRecorder.lapCompleted(lapTime, track.length());
        playerSection = 0;
        bus.publish(new GameEvent.LapCompleted(r.isPlayer(), r.lap(), lapTime, lapTime <= r.bestLap(),
                r.lap() == setup.laps() - 1));
        if (finishedNow) {
            if (phase == Phase.RACING) {
                phase = Phase.FINISHED;
            }
            r.setDriver(new AiDriver(1.0, 0, setup.seed())); // piloto automático en la vuelta de honor
            updatePositions();
            bus.publish(new GameEvent.RaceFinished(r.position()));
        } else if (setup.mode().timeLimited() && phase == Phase.RACING) {
            int sections = track.checkpoints().length;
            double bonus = setup.mode().sectionBonus(track, setup.difficulty(), sections);
            timeLeft += bonus;
            bus.publish(new GameEvent.CheckpointPassed(bonus));
        }
    }

    private void updatePositions() {
        standings.update();
    }

    // ------------------------------------------------------------------ consultas

    public boolean isOver() {
        return endTimer > END_DELAY;
    }

    public Phase phase() {
        return phase;
    }

    public double countdown() {
        return countdown;
    }

    public double timeLeft() {
        return timeLeft;
    }

    public RaceSetup setup() {
        return setup;
    }

    public int laps() {
        return setup.laps();
    }

    public GhostData recordedGhost() {
        return ghostRecorder.best();
    }

    public List<Racer> standings() {
        return standings.asList();
    }

    /** Tiempo de la vuelta en curso del jugador. */
    public double currentLapTime() {
        return player.lap() < 0 ? elapsed : elapsed - player.lapStart();
    }

    public List<RaceResult> results() {
        double len = track.length();
        List<RaceResult> list = new ArrayList<>();
        for (Racer r : standings.asList()) {
            double total;
            if (r.finished()) {
                total = r.finishTime();
            } else {
                double done = Math.max(1, r.progress(len));
                double remaining = Math.max(0, setup.laps() * len - done);
                double avg = done / Math.max(1, elapsed);
                total = elapsed + remaining / Math.max(1, avg);
            }
            CarSpec s = r.car().spec();
            list.add(new RaceResult(r.name(), r.isPlayer(), r.position(), total, r.bestLap(), r.finished(),
                    s.name(), s.number(), s.bodyRgb()));
        }
        return list;
    }

    @Override
    public Track track() {
        return track;
    }

    @Override
    public List<Racer> racers() {
        return readOnlyRacers;
    }

    @Override
    public Racer player() {
        return player;
    }

    @Override
    public double elapsed() {
        return elapsed;
    }
}
