package com.daytona.screens;

import com.daytona.core.Log;

import com.daytona.core.Screen;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.race.GhostData;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.RaceResult;
import com.daytona.domain.race.RaceSetup;
import com.daytona.persistence.LapRecord;
import com.daytona.persistence.RaceResultEntry;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;
import com.daytona.screens.ui.NameEntry;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

/** Tabla final, ingreso de nombre si hay récord y guardado en la base de datos. */
public final class ResultsScreen implements Screen {

    private static final Log LOG = Log.of("results");
    private static final int TOP = 10;

    private final GameContext ctx;
    private final RaceSession session;
    private final Race race;
    private final RaceSetup setup;
    private final MenuList menu = new MenuList();
    private List<RaceResult> results;
    private RaceResult player;
    private NameEntry nameEntry;
    private boolean qualifiesTop;
    private boolean lapRecord;
    private String savedMessage = "";
    private double time;

    public ResultsScreen(GameContext ctx, RaceSession session, Race race) {
        this.ctx = ctx;
        this.session = session;
        this.race = race;
        this.setup = race.setup();
    }

    @Override
    public void onEnter() {
        results = race.results();
        player = results.stream().filter(RaceResult::player).findFirst().orElse(results.get(0));
        updateStats();
        if (session.isChampionship()) {
            session.championship().addResults(results);
        }
        try {
            if (player.finished()) {
                qualifiesTop = ctx.records().qualifies(setup.track().id(), setup.track().mirror(), setup.mode().type().name(),
                        Math.round(player.totalTime() * 1000), TOP);
            }
            GhostData ghost = race.recordedGhost();
            if (ghost != null) {
                Optional<LapRecord> current = ctx.records().bestLap(setup.track().id(), setup.track().mirror());
                lapRecord = !current.isPresent() || Math.round(ghost.lapTime() * 1000) < current.get().lapMs();
            }
        } catch (RuntimeException e) {
            LOG.warn("Récords no disponibles: " + e.getMessage());
        }
        if (qualifiesTop || lapRecord) {
            nameEntry = new NameEntry(ctx.settings().playerName());
            ctx.input().keyboard().clearTyped();
        } else {
            buildMenu();
        }
    }

    private void updateStats() {
        ctx.settings().addStat("races", 1);
        if (player.position() == 1 && player.finished() && results.size() > 1) {
            ctx.settings().addStat("wins", 1);
        }
        double meters = race.player().progress(setup.track().length()) * com.daytona.domain.track.TrackConstants.METERS_PER_UNIT;
        ctx.settings().addStat("meters", Math.max(0, Math.round(meters)));
    }

    private void save(String name) {
        ctx.settings().setPlayerName(name);
        try {
            if (qualifiesTop) {
                ctx.records().saveResult(new RaceResultEntry(setup.track().id(), setup.track().mirror(), setup.mode().type().name(),
                        setup.playerCar().id(), setup.transmission().shortLabel(), name, player.position(),
                        Math.round(player.totalTime() * 1000), Math.round(Math.max(0, player.bestLap()) * 1000),
                        System.currentTimeMillis()));
            }
            GhostData ghost = race.recordedGhost();
            if (lapRecord && ghost != null) {
                ctx.records().saveLapIfBetter(new LapRecord(setup.track().id(), setup.track().mirror(),
                        Math.round(ghost.lapTime() * 1000), name, setup.playerCar().id(), ghost.toBytes()));
            }
            savedMessage = "Récord guardado en " + ctx.database().description();
        } catch (RuntimeException e) {
            savedMessage = "No se pudo guardar: " + e.getMessage();
        }
    }

    private void buildMenu() {
        menu.clear();
        if (session.isChampionship()) {
            menu.add(MenuList.action("Ver campeonato", () -> ctx.nav().championship(session)));
        } else {
            menu.add(MenuList.action("Reintentar", () -> ctx.nav().race(session)));
            menu.add(MenuList.action("Cambiar pista", () -> ctx.nav().trackSelect(session)));
            menu.add(MenuList.action("Cambiar auto", () -> ctx.nav().carSelect(session)));
        }
        menu.add(MenuList.action("Menú principal", () -> ctx.nav().mainMenu()));
    }

    @Override
    public void update(double dt) {
        time += dt;
        if (nameEntry != null) {
            nameEntry.update(ctx.input(), dt);
            if (nameEntry.done()) {
                save(nameEntry.value());
                nameEntry = null;
                buildMenu();
                ctx.input().consumeAll();
            }
            return;
        }
        if (menu.update(ctx.input(), dt)) {
            ctx.audio().beep(660, 0.04);
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double s = UiKit.scale(h);
        UiKit.menuBackground(g, w, h, time);
        UiKit.smooth(g);
        String title = race.phase() == Race.Phase.TIME_OVER ? "TIEMPO AGOTADO"
                : player.position() == 1 && results.size() > 1 ? "¡GANASTE!" : "RESULTADOS";
        UiKit.outlinedText(g, title, w / 2.0, 58 * s, UiKit.display((float) (44 * s)),
                race.phase() == Race.Phase.TIME_OVER ? UiKit.BAD : UiKit.ACCENT, UiKit.Align.CENTER);
        UiKit.text(g, setup.track().name() + (setup.track().mirror() ? " (espejo)" : "") + "  ·  " + setup.mode().type().label()
                        + "  ·  " + setup.playerCar().name() + " " + setup.transmission().shortLabel(),
                w / 2.0, 82 * s, UiKit.body((float) (14 * s)), UiKit.MUTED, UiKit.Align.CENTER);

        double tx = w * 0.06;
        double tw = w * 0.5;
        double rowH = Math.min(26 * s, (h - 150 * s) / Math.max(10, results.size() + 1));
        UiKit.panel(g, tx, 96 * s, tw, rowH * (Math.min(results.size(), 16) + 1.4));
        double y = 96 * s + rowH;
        float fs = (float) (rowH * 0.55);
        UiKit.text(g, "POS", tx + 12 * s, y, UiKit.bodyBold(fs), UiKit.MUTED, UiKit.Align.LEFT);
        UiKit.text(g, "PILOTO", tx + 60 * s, y, UiKit.bodyBold(fs), UiKit.MUTED, UiKit.Align.LEFT);
        UiKit.text(g, "TIEMPO", tx + tw * 0.62, y, UiKit.bodyBold(fs), UiKit.MUTED, UiKit.Align.RIGHT);
        UiKit.text(g, "MEJOR V.", tx + tw - 12 * s, y, UiKit.bodyBold(fs), UiKit.MUTED, UiKit.Align.RIGHT);
        for (int i = 0; i < Math.min(results.size(), 16); i++) {
            RaceResult r = results.get(i);
            y += rowH;
            Color c = r.player() ? UiKit.ACCENT : UiKit.TEXT;
            g.setColor(new Color(r.bodyRgb()));
            g.fillRect((int) (tx + 44 * s), (int) (y - rowH * 0.55), (int) (8 * s), (int) (rowH * 0.6));
            UiKit.text(g, UiKit.ordinal(r.position()), tx + 12 * s, y, UiKit.bodyBold(fs), c, UiKit.Align.LEFT);
            UiKit.text(g, r.name() + "  #" + r.number(), tx + 60 * s, y, UiKit.bodyBold(fs), c, UiKit.Align.LEFT);
            String total = r.finished() ? UiKit.formatTime(r.totalTime()) : "~" + UiKit.formatTime(r.totalTime());
            if (r.player() && !r.finished()) {
                total = "NO TERMINÓ";
            }
            UiKit.text(g, total, tx + tw * 0.62, y, UiKit.bodyBold(fs), c, UiKit.Align.RIGHT);
            UiKit.text(g, UiKit.formatTime(r.bestLap()), tx + tw - 12 * s, y, UiKit.body(fs), c, UiKit.Align.RIGHT);
        }

        double px = w * 0.6;
        double pw = w * 0.34;
        if (nameEntry != null) {
            UiKit.panel(g, px, 110 * s, pw, 240 * s);
            UiKit.shadowText(g, lapRecord ? "¡NUEVO RÉCORD DE VUELTA!" : "¡ENTRASTE AL TOP 10!", px + pw / 2, 150 * s,
                    UiKit.display((float) (20 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
            UiKit.text(g, "Ingresá tus iniciales", px + pw / 2, 176 * s, UiKit.body((float) (14 * s)), UiKit.TEXT, UiKit.Align.CENTER);
            nameEntry.render(g, px + pw / 2, 196 * s, s);
            UiKit.text(g, "↑↓ letra  ←→ mover  ENTER confirmar  (o tipeá)", px + pw / 2, 330 * s, UiKit.body((float) (12 * s)),
                    UiKit.MUTED, UiKit.Align.CENTER);
        } else {
            menu.render(g, px, 120 * s, pw, 40 * s, s);
            UiKit.text(g, savedMessage, px, 120 * s + menu.size() * 40 * s + 20 * s, UiKit.body((float) (12 * s)), UiKit.GOOD,
                    UiKit.Align.LEFT);
            if (session.isChampionship()) {
                int pts = com.daytona.domain.race.ChampionshipSession.pointsFor(player.position());
                UiKit.shadowText(g, "+" + pts + " puntos", px, 120 * s + menu.size() * 40 * s + 50 * s,
                        UiKit.display((float) (22 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
            }
            if (setup.mode().type() == GameModeType.TIME_TRIAL && race.recordedGhost() != null) {
                UiKit.text(g, "Tu mejor vuelta quedó como fantasma para la próxima.", px, h - 40 * s,
                        UiKit.body((float) (12 * s)), UiKit.MUTED, UiKit.Align.LEFT);
            }
        }
    }
}
