package com.daytona.screens;

import com.daytona.core.Screen;
import com.daytona.domain.race.ChampionshipSession;
import com.daytona.input.MenuAction;
import com.daytona.render.UiKit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Tabla de puntos del campeonato entre carreras y al final. */
public final class ChampionshipScreen implements Screen {

    private final GameContext ctx;
    private final RaceSession session;
    private double time;

    public ChampionshipScreen(GameContext ctx, RaceSession session) {
        this.ctx = ctx;
        this.session = session;
    }

    @Override
    public void update(double dt) {
        time += dt;
        ChampionshipSession c = session.championship();
        if (ctx.input().pressed(MenuAction.CONFIRM)) {
            if (c.isComplete()) {
                if (c.playerRank() == 1) {
                    ctx.settings().addStat("titles", 1);
                }
                ctx.nav().mainMenu();
            } else {
                ctx.nav().race(session);
            }
        } else if (ctx.input().pressed(MenuAction.BACK)) {
            ctx.nav().mainMenu();
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double s = UiKit.scale(h);
        UiKit.menuBackground(g, w, h, time);
        UiKit.smooth(g);
        ChampionshipSession c = session.championship();
        boolean done = c.isComplete();
        String title = done ? (c.playerRank() == 1 ? "¡CAMPEÓN!" : "CAMPEONATO TERMINADO") : "CAMPEONATO";
        UiKit.outlinedText(g, title, w / 2.0, 60 * s, UiKit.display((float) (44 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
        String sub = done ? "Terminaste " + UiKit.ordinal(c.playerRank()) + " en la general"
                : "Próxima: ronda " + c.round() + " de " + c.rounds() + "  ·  " + ctx.tracks().get(c.currentTrackId(), false).name();
        UiKit.text(g, sub, w / 2.0, 88 * s, UiKit.bodyBold((float) (16 * s)), UiKit.TEXT, UiKit.Align.CENTER);

        List<ChampionshipSession.Standing> table = c.standings();
        double tw = w * 0.5;
        double tx = (w - tw) / 2;
        double rowH = Math.min(28 * s, (h - 170 * s) / Math.max(8, table.size()));
        UiKit.panel(g, tx, 104 * s, tw, rowH * (table.size() + 0.8));
        double y = 104 * s + rowH * 0.9;
        float fs = (float) (rowH * 0.55);
        for (int i = 0; i < table.size(); i++) {
            ChampionshipSession.Standing st = table.get(i);
            Color col = st.player() ? UiKit.ACCENT : UiKit.TEXT;
            g.setColor(new Color(st.bodyRgb()));
            g.fillRect((int) (tx + 50 * s), (int) (y - rowH * 0.55), (int) (8 * s), (int) (rowH * 0.6));
            UiKit.text(g, UiKit.ordinal(i + 1), tx + 14 * s, y, UiKit.bodyBold(fs), col, UiKit.Align.LEFT);
            UiKit.text(g, st.name(), tx + 70 * s, y, UiKit.bodyBold(fs), col, UiKit.Align.LEFT);
            UiKit.text(g, st.points() + " pts", tx + tw - 14 * s, y, UiKit.bodyBold(fs), col, UiKit.Align.RIGHT);
            y += rowH;
        }
        UiKit.text(g, done ? "ENTER / A para volver al menú" : "ENTER / A para la próxima carrera   ·   ESC abandonar",
                w / 2.0, h - 16 * s, UiKit.body((float) (13 * s)), UiKit.MUTED, UiKit.Align.CENTER);
    }
}
