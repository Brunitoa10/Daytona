package com.daytona.screens;

import com.daytona.core.Screen;
import com.daytona.domain.car.CarSpec;
import com.daytona.domain.race.GameMode;
import com.daytona.domain.race.GhostData;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.RaceSetup;
import com.daytona.domain.race.SuppliedDriver;
import com.daytona.domain.track.Track;
import com.daytona.input.joystick.JoystickInfo;

/**
 * Navegación entre pantallas (Factory de pantallas). Centraliza el flujo del juego
 * para que ninguna pantalla tenga que conocer cómo se construye otra.
 */
public final class Navigator {

    private GameContext ctx;

    public void bind(GameContext ctx) {
        this.ctx = ctx;
    }

    private void go(Screen s) {
        ctx.input().consumeAll();
        ctx.screens().show(s);
    }

    public void title() {
        go(new TitleScreen(ctx));
    }

    public void mainMenu() {
        go(new MainMenuScreen(ctx));
    }

    public void carSelect(RaceSession session) {
        go(new CarSelectScreen(ctx, session));
    }

    public void trackSelect(RaceSession session) {
        go(new TrackSelectScreen(ctx, session));
    }

    public void race(RaceSession session) {
        ctx.input().refreshSource();
        go(new RaceScreen(ctx, session, buildSetup(session)));
    }

    public void results(RaceSession session, Race race) {
        go(new ResultsScreen(ctx, session, race));
    }

    public void championship(RaceSession session) {
        go(new ChampionshipScreen(ctx, session));
    }

    public void controls(Screen back) {
        go(new ControlsScreen(ctx, back));
    }

    public void wizard(JoystickInfo device, Screen back) {
        go(new BindingWizardScreen(ctx, device, back));
    }

    public void options(Screen back) {
        go(new OptionsScreen(ctx, back));
    }

    public void records() {
        go(new RecordsScreen(ctx));
    }

    public void back(Screen screen) {
        go(screen);
    }

    private RaceSetup buildSetup(RaceSession session) {
        Track track = ctx.tracks().get(session.trackId(), session.mirror());
        CarSpec car = ctx.cars().byId(session.carId());
        GameMode mode = session.mode().create();
        int laps = ctx.settings().raceLength().apply(track.info().defaultLaps());
        GhostData ghost = null;
        if (mode.ghostEnabled()) {
            try {
                ghost = ctx.records().bestLap(track.id(), track.mirror())
                        .flatMap(l -> GhostData.fromBytes(l.ghost())).orElse(null);
            } catch (RuntimeException ignored) {
                // sin fantasma
            }
        }
        long seed = session.isChampionship() ? session.championship().seedForRound() : System.nanoTime();
        return new RaceSetup(track, car, ctx.settings().transmission(), mode, laps, mode.rivals(ctx.settings().rivals()),
                ctx.settings().difficulty(), ctx.settings().playerName(), new SuppliedDriver(ctx.input()::drive),
                ghost, ctx.cars(), seed);
    }
}
