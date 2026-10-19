package com.daytona.screens;

import com.daytona.domain.race.ChampionshipSession;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.track.TrackInfo;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Graphics2D;
import java.util.List;

/** Menú principal: elección del modo de juego y accesos a controles, opciones y récords. */
public final class MainMenuScreen extends MenuScreen {

    public MainMenuScreen(GameContext ctx) {
        super(ctx);
    }

    @Override
    protected String title() {
        return "MODO DE JUEGO";
    }

    @Override
    protected void buildItems(MenuList list) {
        for (GameModeType mode : GameModeType.values()) {
            list.add(MenuList.action(mode.label(), () -> start(mode)));
        }
        list.add(MenuList.action("Controles  (teclado / joystick / volante)", () -> ctx.nav().controls(this)));
        list.add(MenuList.action("Opciones", () -> ctx.nav().options(this)));
        list.add(MenuList.action("Récords", () -> ctx.nav().records()));
        list.add(MenuList.action("Salir", () -> ctx.display().requestQuit()));
    }

    private void start(GameModeType mode) {
        RaceSession session = new RaceSession(mode, ctx.settings().lastCar(), ctx.settings().lastTrack());
        if (mode == GameModeType.CHAMPIONSHIP) {
            List<String> ids = ctx.tracks().all().stream().map(TrackInfo::id).collect(java.util.stream.Collectors.toList());
            session.setChampionship(new ChampionshipSession(ids, false));
        }
        ctx.nav().carSelect(session);
    }

    @Override
    protected void onBack() {
        ctx.nav().title();
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double s) {
        int sel = list.selected();
        double x = w * 0.58;
        double y = 110 * s;
        UiKit.panel(g, x, y, w * 0.36, 230 * s);
        if (sel < GameModeType.values().length) {
            GameModeType m = GameModeType.values()[sel];
            UiKit.shadowText(g, m.label().toUpperCase(), x + 18 * s, y + 42 * s, UiKit.display((float) (28 * s)), UiKit.ACCENT,
                    UiKit.Align.LEFT);
            UiKit.wrapText(g, m.description(), x + 18 * s, y + 76 * s, w * 0.32, s);
        } else {
            UiKit.shadowText(g, "DAYTONA", x + 18 * s, y + 42 * s, UiKit.display((float) (28 * s)), UiKit.ACCENT,
                    UiKit.Align.LEFT);
            UiKit.wrapText(g, "Control actual: " + ctx.input().sourceDescription(), x + 18 * s, y + 76 * s, w * 0.32, s);
        }
        long races = ctx.settings().stat("races");
        long wins = ctx.settings().stat("wins");
        long km = ctx.settings().stat("meters") / 1000;
        UiKit.text(g, "Carreras: " + races + "   Victorias: " + wins + "   Km: " + km, x + 18 * s, y + 212 * s,
                UiKit.body((float) (13 * s)), UiKit.MUTED, UiKit.Align.LEFT);
    }
}
