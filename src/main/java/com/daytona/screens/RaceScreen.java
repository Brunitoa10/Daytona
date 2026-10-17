package com.daytona.screens;

import com.daytona.audio.RaceSoundEffects;
import com.daytona.core.EventBus;
import com.daytona.core.GameEvent;
import com.daytona.core.Screen;
import com.daytona.domain.car.Car;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.RaceSetup;
import com.daytona.domain.race.Racer;
import com.daytona.input.MenuAction;
import com.daytona.render.HudRenderer;
import com.daytona.render.RaceView;
import com.daytona.render.UiKit;
import com.daytona.render.theme.VisualTheme;
import com.daytona.screens.ui.MenuList;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** La carrera en sí: física, render, HUD, sonido y menú de pausa. */
public final class RaceScreen implements Screen {

    private final GameContext ctx;
    private final RaceSession session;
    private final RaceSetup setup;
    private final EventBus bus = new EventBus();
    private final EventBus.Subscriptions subs = new EventBus.Subscriptions();
    private final MenuList pauseMenu = new MenuList();
    private Race race;
    private RaceView view;
    private HudRenderer hud;
    private RaceSoundEffects sfx;
    private boolean paused;
    private boolean leaving;

    public RaceScreen(GameContext ctx, RaceSession session, RaceSetup setup) {
        this.ctx = ctx;
        this.session = session;
        this.setup = setup;
    }

    @Override
    public void onEnter() {
        race = new Race(setup, bus);
        VisualTheme theme = ctx.themes().current();
        view = new RaceView(ctx.atlas(), ctx.background(), theme.createCarRenderer(ctx.atlas()));
        view.camera().setMode(ctx.settings().viewMode());
        view.setGhost(setup.ghost());
        hud = new HudRenderer(bus, ctx.settings(), ctx.minimap(), theme.createHudSkin(ctx.settings()));
        sfx = new RaceSoundEffects(bus, ctx.audio());
        subs.add(bus.subscribe(GameEvent.Collision.class, e -> view.camera().addShake(e.intensity() * 0.8)));
        subs.add(bus.subscribe(GameEvent.WallScrape.class, e -> view.camera().addShake(0.2)));
        List<com.daytona.domain.car.CarSpec> specs = new ArrayList<>();
        for (Racer r : race.racers()) {
            specs.add(r.car().spec());
        }
        ctx.atlas().warmUp(race.track(), specs);
        if (setup.ghost() != null) {
            hud.show("FANTASMA  " + UiKit.formatTime(setup.ghost().lapTime()), UiKit.MUTED, 4, false);
        }
        if (ctx.input().usingFallback()) {
            hud.show("No se detectó el " + ctx.settings().controlMode().label().toLowerCase() + ": usando teclado",
                    UiKit.BAD, 5, false);
        }
        buildPauseMenu();
    }

    private void buildPauseMenu() {
        pauseMenu.clear();
        pauseMenu.add(MenuList.action("Continuar", () -> paused = false));
        pauseMenu.add(MenuList.action("Reiniciar carrera", () -> {
            leaving = true;
            ctx.nav().race(session);
        }));
        pauseMenu.add(MenuList.choice("Cámara", () -> view.camera().mode().label(), d -> cycleView()));
        pauseMenu.add(MenuList.action("Controles", () -> {
            leaving = true;
            ctx.nav().controls(new MainMenuScreen(ctx));
        }));
        pauseMenu.add(MenuList.action("Abandonar", () -> {
            leaving = true;
            ctx.nav().mainMenu();
        }));
    }

    private void cycleView() {
        view.camera().setMode(view.camera().mode().next());
        ctx.settings().setViewMode(view.camera().mode());
    }

    @Override
    public void update(double dt) {
        if (leaving) {
            return;
        }
        if (paused) {
            ctx.audio().stopLoops();
            if (pauseMenu.update(ctx.input(), dt)) {
                ctx.audio().beep(660, 0.04);
            } else if (ctx.input().pressed(MenuAction.BACK) || ctx.input().pressed(MenuAction.PAUSE)) {
                paused = false;
            }
            return;
        }
        if (ctx.input().pausePressed() && race.phase() != Race.Phase.FINISHED) {
            paused = true;
            pauseMenu.select(0);
            ctx.input().consumeAll();
            return;
        }
        race.update(dt);
        view.update(race, race.player(), dt);
        hud.update(dt);
        if (ctx.input().viewPressed()) {
            cycleView();
        }
        Car car = race.player().car();
        boolean over = race.phase() == Race.Phase.TIME_OVER;
        ctx.audio().setEngine(car.rpm(), over ? 0 : car.throttle(), true);
        double skid = car.drifting() ? 0.9 : car.offRoad() ? 0.5 * car.speedRatio() : 0;
        if (car.braking() && car.speedRatio() > 0.5 && Math.abs(car.steerVisual()) > 0.6) {
            skid = Math.max(skid, 0.6);
        }
        ctx.audio().setSkid(skid);
        ctx.audio().setScrape(car.scraping() ? car.speedRatio() : 0);
        if (race.isOver()) {
            leaving = true;
            ctx.nav().results(session, race);
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double a = paused || leaving ? 1 : alpha;
        view.render(g, w, h, race, race.player(), a, ctx.settings().drawDistance());
        hud.render(g, w, h, race, race.player(), ctx.display().fps());
        if (paused) {
            double s = UiKit.scale(h);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, w, h);
            UiKit.outlinedText(g, "PAUSA", w / 2.0, h * 0.3, UiKit.display((float) (54 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
            pauseMenu.render(g, w * 0.32, h * 0.36, w * 0.36, 40 * s, s);
        }
    }

    @Override
    public void onExit() {
        ctx.audio().stopLoops();
        subs.close();
        hud.close();
        sfx.close();
    }
}
