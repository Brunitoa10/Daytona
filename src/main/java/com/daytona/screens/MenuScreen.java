package com.daytona.screens;

import com.daytona.core.Screen;
import com.daytona.input.MenuAction;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Graphics2D;

/**
 * Patrón Template Method: esqueleto común de los menús (fondo, título, lista,
 * ayuda). Las subclases definen las opciones y, si quieren, un panel extra.
 */
public abstract class MenuScreen implements Screen {

    protected final GameContext ctx;
    protected final MenuList list = new MenuList();
    protected double time;

    protected MenuScreen(GameContext ctx) {
        this.ctx = ctx;
    }

    protected abstract String title();

    protected abstract void buildItems(MenuList list);

    /** Acción al volver (Esc / B). */
    protected abstract void onBack();

    /** Ancho de la lista como fracción de la pantalla. */
    protected double listWidth() {
        return 0.46;
    }

    protected double listX(int w) {
        return w * 0.07;
    }

    protected void renderExtra(Graphics2D g, int w, int h, double s) {
    }

    protected String footer() {
        return "↑↓ elegir   ←→ cambiar   ENTER / A confirmar   ESC / B volver";
    }

    @Override
    public void onEnter() {
        rebuild(); // al volver a esta pantalla se conserva la opción elegida
    }

    /** Reconstruye la lista manteniendo la selección. */
    protected void rebuild() {
        int sel = list.selected();
        list.clear();
        buildItems(list);
        list.select(sel);
    }

    @Override
    public void update(double dt) {
        time += dt;
        if (list.update(ctx.input(), dt)) {
            ctx.audio().beep(660, 0.04);
            return;
        }
        if (ctx.input().pressed(MenuAction.BACK)) {
            ctx.audio().beep(330, 0.06);
            onBack();
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double s = UiKit.scale(h);
        UiKit.menuBackground(g, w, h, time);
        UiKit.smooth(g);
        UiKit.outlinedText(g, title(), listX(w), 64 * s, UiKit.display((float) (40 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
        double rowH = Math.min(40 * s, (h - 150 * s) / Math.max(1, list.size()));
        list.render(g, listX(w), 92 * s, w * listWidth(), rowH, s);
        renderExtra(g, w, h, s);
        UiKit.text(g, footer(), w / 2.0, h - 14 * s, UiKit.body((float) (12 * s)), UiKit.MUTED, UiKit.Align.CENTER);
    }
}
