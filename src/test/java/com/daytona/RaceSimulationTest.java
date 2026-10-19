package com.daytona;

import com.daytona.core.EventBus;
import com.daytona.core.GameEvent;
import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.race.AiDriver;
import com.daytona.domain.race.Difficulty;
import com.daytona.domain.race.Driver;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.RaceResult;
import com.daytona.domain.race.RaceSetup;
import com.daytona.domain.race.Racer;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackCatalog;
import com.daytona.domain.track.TrackInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Simula carreras completas para validar IA, vueltas, checkpoints y tiempos del arcade. */
public class RaceSimulationTest {

    private static final double DT = 1.0 / 120;
    private final TrackCatalog tracks = new TrackCatalog();
    private final CarCatalog cars = new CarCatalog();

    private Race run(Track track, GameModeType mode, int laps, int rivals, Driver player, EventBus bus) {
        RaceSetup setup = new RaceSetup(track, cars.byId("falcon"), TransmissionType.AUTOMATIC, mode.create(), laps, rivals,
                Difficulty.NORMAL, "TST", player, null, cars, 42);
        Race race = new Race(setup, bus);
        for (int i = 0; i < 120 * 60 * 12 && (player == null || !race.isOver()); i++) {
            race.update(DT);
            if (player == null && race.racers().stream().allMatch(Racer::finished)) {
                break;
            }
        }
        return race;
    }

    @Test
    public void aiFinishesEveryTrack() {
        for (TrackInfo info : tracks.all()) {
            Race race = run(tracks.get(info.id(), false), GameModeType.CHAMPIONSHIP, 2, 7, null, new EventBus());
            List<RaceResult> results = race.results();
            long finished = results.stream().filter(RaceResult::finished).count();
            assertTrue(finished >= 7, info.id() + ": terminaron " + finished);
            double lap = results.get(0).bestLap();
            System.out.printf("%s: vuelta IA %.2fs, ganador %.2fs%n", info.id(), lap, results.get(0).totalTime());
            assertTrue(lap > 10 && lap < 200, "vuelta razonable: " + lap);
        }
    }

    @Test
    public void arcadeTimeIsEnoughForAGoodDriver() {
        for (TrackInfo info : tracks.all()) {
            EventBus bus = new EventBus();
            AtomicInteger checkpoints = new AtomicInteger();
            AtomicInteger timeOver = new AtomicInteger();
            bus.subscribe(GameEvent.CheckpointPassed.class, e -> checkpoints.incrementAndGet());
            bus.subscribe(GameEvent.TimeOver.class, e -> timeOver.incrementAndGet());
            AiDriver brain = new AiDriver(1.0, 0, 1);
            Driver human = brain::decide;
            Race race = run(tracks.get(info.id(), false), GameModeType.ARCADE, info.defaultLaps(), 11, human, bus);
            System.out.printf("%s arcade: checkpoints=%d tiempo restante=%.1f fase=%s%n", info.id(), checkpoints.get(),
                    race.timeLeft(), race.phase());
            assertTrue(checkpoints.get() > 0);
            assertFalse(timeOver.get() > 0, info.id() + ": un piloto bueno no debería quedarse sin tiempo");
        }
    }
}
