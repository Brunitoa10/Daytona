package com.daytona.render;

import com.daytona.config.Settings;
import com.daytona.core.EventBus;
import com.daytona.core.GameEvent;
import com.daytona.render.hud.HudSkin;
import com.daytona.domain.car.Car;
import com.daytona.domain.race.Race;
import com.daytona.domain.race.Racer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * HUD: escucha los eventos de la carrera (Observer) para mostrar mensajes y delega los
 * indicadores en un {@link HudSkin} intercambiable (Strategy).
 */
public final class HudRenderer implements AutoCloseable {

    private static final class Message {
        final String text;
        final Color color;
        final double duration;
        final boolean big;
        double time;

        Message(String text, Color color, double duration, boolean big) {
            this.text = text;
            this.color = color;
            this.duration = duration;
            this.big = big;
        }
    }

    private final Settings settings;
    private final MinimapRenderer minimap;
    private final EventBus.Subscriptions subs = new EventBus.Subscriptions();
    private final List<Message> messages = new ArrayList<>();
    private double clock;
    private HudSkin skin;

    public HudRenderer(EventBus bus, Settings settings, MinimapRenderer minimap, HudSkin skin) {
        this.skin = skin;
        this.settings = settings;
        this.minimap = minimap;
        subs.add(bus.subscribe(GameEvent.CheckpointPassed.class,
                e -> show("¡CHECKPOINT!  +" + (int) Math.round(e.bonusSeconds()) + " s", UiKit.GOOD, 2.2, false)));
        subs.add(bus.subscribe(GameEvent.LapCompleted.class, e -> {
            if (!e.player()) {
                return;
            }
            if (e.bestLap()) {
                show("MEJOR VUELTA  " + UiKit.formatTime(e.lapTime()), UiKit.ACCENT, 2.5, false);
            } else {
                show("VUELTA  " + UiKit.formatTime(e.lapTime()), UiKit.TEXT, 2.0, false);
            }
            if (e.finalLapNext()) {
                show("ÚLTIMA VUELTA", UiKit.ACCENT2, 2.5, true);
            }
        }));
        subs.add(bus.subscribe(GameEvent.RaceStarted.class, e -> {
            if (e.perfectStart()) {
                show("¡LARGADA PERFECTA!", UiKit.GOOD, 2.0, false);
            }
        }));
        subs.add(bus.subscribe(GameEvent.TimeOver.class, e -> show("TIEMPO AGOTADO", UiKit.BAD, 6, true)));
        subs.add(bus.subscribe(GameEvent.RaceFinished.class,
                e -> show("¡META!  " + UiKit.ordinal(e.playerPosition()), UiKit.ACCENT, 6, true)));
    }

    public void show(String text, Color color, double seconds, boolean big) {
        messages.add(new Message(text, color, seconds, big));
        if (messages.size() > 4) {
            messages.remove(0);
        }
    }

    public void update(double dt) {
        clock += dt;
        messages.removeIf(m -> (m.time += dt) > m.duration);
    }

    public void render(Graphics2D g, int w, int h, Race race, Racer player, double fps) {
        UiKit.smooth(g);
        double s = UiKit.scale(h);
        Car car = player.car();

        skin.drawPanels(g, w, h, s, race, player, clock);
        minimap.render(g, race.track(), 14 * s, h - 150 * s, (int) (136 * s), race.racers(), -1);

        // ---- indicadores ----
        if (car.slipstream() && race.phase() == Race.Phase.RACING) {
            UiKit.shadowText(g, "REBUFO", w - 110 * s, h - 168 * s, UiKit.display((float) (18 * s)),
                    clock % 0.4 < 0.2 ? UiKit.ACCENT : UiKit.TEXT, UiKit.Align.CENTER);
        }
        if (car.drifting()) {
            UiKit.shadowText(g, "DERRAPE", w / 2.0, h - 24 * s, UiKit.display((float) (18 * s)), UiKit.ACCENT2, UiKit.Align.CENTER);
        }

        drawCountdown(g, w, h, s, race);
        drawMessages(g, w, h, s);
        if (settings.showFps()) {
            UiKit.shadowText(g, String.format("%.0f FPS", fps), w - 10 * s, h - 6 * s, UiKit.body((float) (11 * s)),
                    UiKit.MUTED, UiKit.Align.RIGHT);
        }
    }

    private void drawCountdown(Graphics2D g, int w, int h, double s, Race race) {
        if (race.phase() == Race.Phase.COUNTDOWN) {
            int n = (int) Math.ceil(race.countdown());
            double frac = race.countdown() - Math.floor(race.countdown());
            float size = (float) ((90 + 50 * frac) * s);
            UiKit.outlinedText(g, Integer.toString(Math.max(1, n)), w / 2.0, h * 0.42, UiKit.display(size), UiKit.ACCENT, UiKit.Align.CENTER);
            UiKit.shadowText(g, "Acelerá a media potencia para una largada perfecta", w / 2.0, h * 0.52,
                    UiKit.bodyBold((float) (14 * s)), UiKit.TEXT, UiKit.Align.CENTER);
        } else if (race.elapsed() < 0.9) {
            UiKit.outlinedText(g, "¡YA!", w / 2.0, h * 0.42, UiKit.display((float) (110 * s)), UiKit.GOOD, UiKit.Align.CENTER);
        }
    }

    private void drawMessages(Graphics2D g, int w, int h, double s) {
        double y = h * 0.3;
        for (Message m : messages) {
            double fade = Math.min(1, (m.duration - m.time) * 3);
            if (fade <= 0) {
                continue;
            }
            Color c = new Color(m.color.getRed(), m.color.getGreen(), m.color.getBlue(), (int) (255 * fade));
            float size = (float) ((m.big ? 44 : 26) * s);
            UiKit.outlinedText(g, m.text, w / 2.0, y, UiKit.display(size), c, UiKit.Align.CENTER);
            y += size * 1.2;
        }
    }

    @Override
    public void close() {
        subs.close();
    }
}
