package com.daytona.audio;

import com.daytona.core.EventBus;
import com.daytona.core.GameEvent;

/** Observer que traduce eventos de la carrera en efectos de sonido. */
public final class RaceSoundEffects implements AutoCloseable {

    private final EventBus.Subscriptions subs = new EventBus.Subscriptions();

    public RaceSoundEffects(EventBus bus, AudioService audio) {
        subs.add(bus.subscribe(GameEvent.CountdownTick.class, e -> {
            if (e.value() > 0) {
                audio.beep(523, 0.22);
            } else {
                audio.beep(1046, 0.5);
            }
        }));
        subs.add(bus.subscribe(GameEvent.CheckpointPassed.class, e -> {
            audio.beep(784, 0.12);
            audio.beep(1175, 0.3, 0.12);
        }));
        subs.add(bus.subscribe(GameEvent.LapCompleted.class, e -> {
            if (e.player()) {
                audio.beep(880, 0.15);
                audio.beep(1320, 0.25, 0.15);
            }
        }));
        subs.add(bus.subscribe(GameEvent.Collision.class, e -> audio.crash(e.intensity())));
        subs.add(bus.subscribe(GameEvent.WallScrape.class, e -> audio.crash(e.intensity() * 0.4)));
        subs.add(bus.subscribe(GameEvent.TimeOver.class, e -> audio.beep(220, 1.2)));
        subs.add(bus.subscribe(GameEvent.RaceFinished.class, e -> {
            audio.beep(659, 0.2);
            audio.beep(784, 0.2, 0.2);
            audio.beep(988, 0.7, 0.4);
        }));
    }

    @Override
    public void close() {
        subs.close();
    }
}
