package com.daytona.screens;

import com.daytona.core.EventBus;
import com.daytona.core.Screen;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.RaceSetup;
import com.daytona.domain.track.Track;
import com.daytona.input.MenuAction;
import com.daytona.render.RaceView;
import com.daytona.render.UiKit;
import com.daytona.render.ViewMode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Pantalla de título con modo demostración (una carrera de la IA de fondo). */
public final class TitleScreen implements Screen {

    private static final double DEMO_SECONDS = 45;

    private final GameContext ctx;
    private final Random random = new Random();
    private RaceView view;
    private Race demo;
    private double demoTime;
    private double time;
    private int trackIndex;

    public TitleScreen(GameContext ctx) {
        this.ctx = ctx;
        this.trackIndex = random.nextInt(ctx.tracks().size());
    }

    @Override
    public void onEnter() {
        ctx.audio().stopLoops();
        newDemo();
    }

    private void newDemo() {
        Track track = ctx.tracks().get(trackIndex++ % ctx.tracks().size(), random.nextBoolean());
        RaceSetup setup = new RaceSetup(track, ctx.cars().playable().get(random.nextInt(4)),
                ctx.settings().transmission(), GameModeType.CHAMPIONSHIP.create(), 99, 7,
                ctx.settings().difficulty(), "DEMO", null, null, ctx.cars(), random.nextLong());
        demo = new Race(setup, new EventBus());
        view = new RaceView(ctx.atlas(), ctx.background(), ctx.themes().current().createCarRenderer(ctx.atlas()));
        view.camera().setMode(random.nextBoolean() ? ViewMode.CHASE_FAR : ViewMode.CHASE_NEAR);
        demoTime = 0;
    }

    @Override
    public void update(double dt) {
        time += dt;
        demoTime += dt;
        demo.update(dt);
        view.update(demo, demo.player(), dt);
        if (demoTime > DEMO_SECONDS) {
            newDemo();
        }
        if (ctx.input().pressed(MenuAction.CONFIRM) || ctx.input().pressed(MenuAction.PAUSE)) {
            ctx.audio().beep(880, 0.08);
            ctx.audio().beep(1320, 0.18, 0.08);
            ctx.nav().mainMenu();
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double s = UiKit.scale(h);
        view.render(g, w, h, demo, demo.player(), alpha, ctx.settings().drawDistance());
        UiKit.smooth(g);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(0, 0, w, h);
        UiKit.logo(g, w, h * 0.40, 110 * s);
        if (time % 1.0 < 0.65) {
            UiKit.outlinedText(g, "PRESIONÁ ENTER / START", w / 2.0, h * 0.74, UiKit.display((float) (26 * s)), UiKit.TEXT,
                    UiKit.Align.CENTER);
        }
        String ctrl = "Control: " + ctx.input().sourceDescription() + "   ·   Datos: " + ctx.database().description();
        UiKit.text(g, ctrl, w / 2.0, h - 16 * s, UiKit.body((float) (12 * s)), UiKit.MUTED, UiKit.Align.CENTER);
        UiKit.text(g, "DEMO  ·  " + demo.track().name(), 16 * s, 24 * s, UiKit.bodyBold((float) (12 * s)), UiKit.MUTED,
                UiKit.Align.LEFT);
    }
}
