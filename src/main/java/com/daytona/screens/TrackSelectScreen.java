package com.daytona.screens;

import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackInfo;
import com.daytona.persistence.LapRecord;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

/** Elección de pista (con modo espejo) mostrando trazado, nivel y mejor vuelta. */
public final class TrackSelectScreen extends MenuScreen {

    private final RaceSession session;
    private int index;
    private Optional<LapRecord> best = Optional.empty();
    private String bestKey = "";

    public TrackSelectScreen(GameContext ctx, RaceSession session) {
        super(ctx);
        this.session = session;
        this.index = ctx.tracks().indexOf(session.trackId());
    }

    @Override
    protected String title() {
        return "ELEGÍ LA PISTA";
    }

    @Override
    protected double listWidth() {
        return 0.4;
    }

    @Override
    protected void buildItems(MenuList list) {
        List<TrackInfo> all = ctx.tracks().all();
        list.add(MenuList.choice("Pista", () -> all.get(index).name(), d -> index = Math.floorMod(index + d, all.size())));
        list.add(MenuList.choice("Espejo", () -> session.mirror() ? "Sí" : "No", d -> session.setMirror(!session.mirror())));
        list.add(MenuList.choice("Vueltas", () -> Integer.toString(laps()), d -> {
            com.daytona.config.RaceLength[] v = com.daytona.config.RaceLength.values();
            ctx.settings().setRaceLength(v[Math.floorMod(ctx.settings().raceLength().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.action("¡Largar!", this::start));
    }

    private int laps() {
        return ctx.settings().raceLength().apply(ctx.tracks().all().get(index).defaultLaps());
    }

    private void start() {
        TrackInfo t = ctx.tracks().all().get(index);
        session.setTrackId(t.id());
        ctx.settings().setLastTrack(t.id());
        ctx.nav().race(session);
    }

    @Override
    protected void onBack() {
        ctx.nav().carSelect(session);
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double s) {
        Track track = ctx.tracks().get(index, session.mirror());
        String key = track.id() + session.mirror();
        if (!key.equals(bestKey)) {
            bestKey = key;
            try {
                best = ctx.records().bestLap(track.id(), track.mirror());
            } catch (RuntimeException e) {
                best = Optional.empty();
            }
        }
        double px = w * 0.5;
        double pw = w * 0.45;
        double ph = h - 130 * s;
        g.setPaint(new GradientPaint(0, (float) (86 * s), new Color(track.theme().skyTop()), 0, (float) (86 * s + ph),
                new Color(track.theme().skyBottom())));
        g.fillRoundRect((int) px, (int) (86 * s), (int) pw, (int) ph, (int) (16 * s), (int) (16 * s));
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRoundRect((int) px, (int) (86 * s), (int) pw, (int) ph, (int) (16 * s), (int) (16 * s));
        int size = (int) Math.min(pw * 0.6, ph * 0.5);
        g.drawImage(ctx.minimap().outline(track, size), (int) (px + (pw - size) / 2), (int) (96 * s), null);
        double y = 96 * s + size + 44 * s;
        TrackInfo info = track.info();
        UiKit.shadowText(g, info.level(), px + 20 * s, y - 26 * s, UiKit.bodyBold((float) (13 * s)), UiKit.ACCENT2,
                UiKit.Align.LEFT);
        UiKit.shadowText(g, info.name().toUpperCase(), px + 20 * s, y, UiKit.display((float) (26 * s)), UiKit.ACCENT,
                UiKit.Align.LEFT);
        UiKit.wrapText(g, info.description(), px + 20 * s, y + 24 * s, pw - 40 * s, s);
        double km = track.length() * com.daytona.domain.track.TrackConstants.METERS_PER_UNIT / 1000.0;
        UiKit.text(g, String.format("Largo: %.2f km   ·   Vueltas: %d", km, laps()), px + 20 * s, y + 74 * s,
                UiKit.bodyBold((float) (13 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        String rec = best.map(b -> "Récord: " + UiKit.formatTime(b.lapMs() / 1000.0) + "  (" + b.playerName() + ")")
                .orElse("Récord: sin tiempo todavía");
        UiKit.text(g, rec, px + 20 * s, y + 96 * s, UiKit.bodyBold((float) (13 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
    }
}
