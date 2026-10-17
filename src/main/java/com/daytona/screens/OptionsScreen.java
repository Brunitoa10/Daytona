package com.daytona.screens;

import com.daytona.config.RaceLength;
import com.daytona.config.Resolution;
import com.daytona.config.Settings;
import com.daytona.config.SpeedUnit;
import com.daytona.config.VisualStyle;
import com.daytona.core.Screen;
import com.daytona.domain.race.Difficulty;
import com.daytona.render.UiKit;
import com.daytona.render.ViewMode;
import com.daytona.screens.ui.MenuList;

import java.awt.Graphics2D;

/** Opciones de video, sonido y juego. Todo se guarda al instante en la base. */
public final class OptionsScreen extends MenuScreen {

    private static final int[] FPS = {60, 120, 144, 165, 240, 0};

    private final Screen back;

    public OptionsScreen(GameContext ctx, Screen back) {
        super(ctx);
        this.back = back;
    }

    @Override
    protected String title() {
        return "OPCIONES";
    }

    @Override
    protected double listWidth() {
        return 0.56;
    }

    @Override
    protected void buildItems(MenuList list) {
        Settings s = ctx.settings();
        list.add(MenuList.choice("Estilo visual", () -> s.visualStyle().label(), d -> {
            VisualStyle[] v = VisualStyle.values();
            s.setVisualStyle(v[Math.floorMod(s.visualStyle().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Resolución interna", () -> s.resolution().label(), d -> {
            Resolution[] v = Resolution.values();
            s.setResolution(v[Math.floorMod(s.resolution().ordinal() + d, v.length)]);
            ctx.display().applyVideoSettings();
        }));
        list.add(MenuList.choice("Pantalla completa", () -> s.fullscreen() ? "Sí" : "No", d -> ctx.display().toggleFullscreen()));
        list.add(MenuList.choice("Límite de FPS", () -> s.fpsLimit() == 0 ? "Sin límite" : Integer.toString(s.fpsLimit()), d -> {
            int idx = 0;
            for (int i = 0; i < FPS.length; i++) {
                if (FPS[i] == s.fpsLimit()) {
                    idx = i;
                }
            }
            s.setFpsLimit(FPS[Math.floorMod(idx + d, FPS.length)]);
        }));
        list.add(MenuList.choice("Mostrar FPS", () -> s.showFps() ? "Sí" : "No", d -> s.setShowFps(!s.showFps())));
        list.add(MenuList.choice("Distancia de dibujo", () -> Integer.toString(s.drawDistance()),
                d -> s.setDrawDistance(Math.max(150, Math.min(500, s.drawDistance() + d * 50)))));
        list.add(MenuList.choice("Volumen", () -> s.volume() + " %", d -> {
            s.setVolume(s.volume() + d * 10);
            ctx.audio().setVolume(s.volume() / 100.0);
            ctx.audio().beep(880, 0.08);
        }));
        list.add(MenuList.choice("Unidades", () -> s.speedUnit().label(), d -> {
            SpeedUnit[] v = SpeedUnit.values();
            s.setSpeedUnit(v[Math.floorMod(s.speedUnit().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Dificultad", () -> s.difficulty().label(), d -> {
            Difficulty[] v = Difficulty.values();
            s.setDifficulty(v[Math.floorMod(s.difficulty().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Largo de carrera", () -> s.raceLength().label(), d -> {
            RaceLength[] v = RaceLength.values();
            s.setRaceLength(v[Math.floorMod(s.raceLength().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Rivales (arcade)", () -> Integer.toString(s.rivals()), d -> s.setRivals(s.rivals() + d)));
        list.add(MenuList.choice("Cámara inicial", () -> s.viewMode().label(), d -> {
            ViewMode[] v = ViewMode.values();
            s.setViewMode(v[Math.floorMod(s.viewMode().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.action("Volver", this::onBack));
    }

    @Override
    protected void onBack() {
        ctx.nav().back(back);
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double sc) {
        double x = w * 0.66;
        UiKit.panel(g, x, 92 * sc, w * 0.3, 160 * sc);
        UiKit.text(g, "Datos guardados en:", x + 14 * sc, 120 * sc, UiKit.bodyBold((float) (13 * sc)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.text(g, ctx.database().description(), x + 14 * sc, 140 * sc, UiKit.body((float) (12 * sc)), UiKit.ACCENT, UiKit.Align.LEFT);
        UiKit.text(g, "Resolución interna más baja", x + 14 * sc, 172 * sc, UiKit.body((float) (12 * sc)), UiKit.MUTED, UiKit.Align.LEFT);
        UiKit.text(g, "= más FPS en PCs modestas.", x + 14 * sc, 190 * sc, UiKit.body((float) (12 * sc)), UiKit.MUTED, UiKit.Align.LEFT);
        UiKit.text(g, String.format("FPS actuales: %.0f", ctx.display().fps()), x + 14 * sc, 226 * sc,
                UiKit.bodyBold((float) (13 * sc)), UiKit.GOOD, UiKit.Align.LEFT);
    }
}
