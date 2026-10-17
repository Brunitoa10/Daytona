package com.daytona.screens;

import com.daytona.domain.race.GameModeType;
import com.daytona.domain.track.TrackInfo;
import com.daytona.persistence.LapRecord;
import com.daytona.persistence.RaceResultEntry;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

/** Récords guardados: mejor vuelta y top 10 por pista, espejo y modo. */
public final class RecordsScreen extends MenuScreen {

    private int track;
    private boolean mirror;
    private int mode;
    private String loadedKey = "";
    private List<RaceResultEntry> top = java.util.Collections.emptyList();
    private Optional<LapRecord> best = Optional.empty();

    public RecordsScreen(GameContext ctx) {
        super(ctx);
    }

    @Override
    protected String title() {
        return "RÉCORDS";
    }

    @Override
    protected double listWidth() {
        return 0.34;
    }

    @Override
    protected void buildItems(MenuList list) {
        List<TrackInfo> all = ctx.tracks().all();
        GameModeType[] modes = GameModeType.values();
        list.add(MenuList.choice("Pista", () -> all.get(track).name(), d -> track = Math.floorMod(track + d, all.size())));
        list.add(MenuList.choice("Espejo", () -> mirror ? "Sí" : "No", d -> mirror = !mirror));
        list.add(MenuList.choice("Modo", () -> modes[mode].label(), d -> mode = Math.floorMod(mode + d, modes.length)));
        list.add(MenuList.action("Volver", this::onBack));
    }

    @Override
    protected void onBack() {
        ctx.nav().mainMenu();
    }

    private void load() {
        String id = ctx.tracks().all().get(track).id();
        String key = id + mirror + mode;
        if (key.equals(loadedKey)) {
            return;
        }
        loadedKey = key;
        try {
            top = ctx.records().topResults(id, mirror, GameModeType.values()[mode].name(), 10);
            best = ctx.records().bestLap(id, mirror);
        } catch (RuntimeException e) {
            top = java.util.Collections.emptyList();
            best = Optional.empty();
        }
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double s) {
        load();
        double px = w * 0.45;
        double pw = w * 0.5;
        UiKit.panel(g, px, 86 * s, pw, h - 130 * s);
        double y = 120 * s;
        UiKit.shadowText(g, "MEJOR VUELTA", px + 18 * s, y, UiKit.display((float) (20 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
        String lap = best.map(b -> UiKit.formatTime(b.lapMs() / 1000.0) + "   " + b.playerName() + "   "
                + ctx.cars().byId(b.carId()).name()).orElse("—");
        UiKit.text(g, lap, px + pw - 18 * s, y, UiKit.bodyBold((float) (15 * s)), UiKit.TEXT, UiKit.Align.RIGHT);
        y += 36 * s;
        UiKit.shadowText(g, "TOP 10  ·  " + GameModeType.values()[mode].label(), px + 18 * s, y, UiKit.display((float) (18 * s)),
                UiKit.ACCENT, UiKit.Align.LEFT);
        y += 28 * s;
        if (top.isEmpty()) {
            UiKit.text(g, "Todavía no hay tiempos. ¡Salí a la pista!", px + 18 * s, y, UiKit.body((float) (14 * s)), UiKit.MUTED,
                    UiKit.Align.LEFT);
        }
        for (int i = 0; i < top.size(); i++) {
            RaceResultEntry e = top.get(i);
            float fs = (float) (14 * s);
            UiKit.text(g, (i + 1) + ".", px + 18 * s, y, UiKit.bodyBold(fs), UiKit.MUTED, UiKit.Align.LEFT);
            UiKit.text(g, e.playerName(), px + 50 * s, y, UiKit.bodyBold(fs), UiKit.TEXT, UiKit.Align.LEFT);
            UiKit.text(g, ctx.cars().byId(e.carId()).name() + " " + e.transmission(), px + 110 * s, y, UiKit.body(fs), UiKit.MUTED,
                    UiKit.Align.LEFT);
            UiKit.text(g, UiKit.ordinal(e.position()), px + pw * 0.62, y, UiKit.body(fs), UiKit.MUTED, UiKit.Align.RIGHT);
            UiKit.text(g, UiKit.formatTime(e.totalMs() / 1000.0), px + pw - 18 * s, y, UiKit.bodyBold(fs), UiKit.ACCENT,
                    UiKit.Align.RIGHT);
            y += 24 * s;
        }
    }
}
