package com.daytona.screens.ui;

import com.daytona.input.InputManager;
import com.daytona.input.MenuAction;
import com.daytona.render.UiKit;

import java.awt.Color;
import java.awt.Graphics2D;

/** Ingreso de iniciales estilo arcade (flechas/joystick) o tipeando con el teclado. */
public final class NameEntry {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 .-";
    private static final int LENGTH = 3;

    private final char[] name = new char[LENGTH];
    private int cursor;
    private boolean done;
    private double time;

    public NameEntry(String initial) {
        String n = (initial == null ? "AAA" : initial.toUpperCase()) + "   ";
        for (int i = 0; i < LENGTH; i++) {
            char c = n.charAt(i);
            name[i] = CHARS.indexOf(c) >= 0 ? c : 'A';
        }
    }

    public void update(InputManager in, double dt) {
        time += dt;
        if (done) {
            return;
        }
        Character typed;
        while ((typed = in.keyboard().pollTyped()) != null) {
            if (CHARS.indexOf(typed) >= 0) {
                name[cursor] = typed;
                cursor = Math.min(LENGTH - 1, cursor + 1);
            }
        }
        if (in.pressed(MenuAction.UP)) {
            cycle(1);
        } else if (in.pressed(MenuAction.DOWN)) {
            cycle(-1);
        } else if (in.pressed(MenuAction.LEFT)) {
            cursor = Math.max(0, cursor - 1);
        } else if (in.pressed(MenuAction.RIGHT)) {
            cursor = Math.min(LENGTH - 1, cursor + 1);
        } else if (in.pressed(MenuAction.CONFIRM)) {
            if (cursor < LENGTH - 1) {
                cursor++;
            } else {
                done = true;
            }
        }
    }

    private void cycle(int d) {
        int i = CHARS.indexOf(name[cursor]);
        name[cursor] = CHARS.charAt(Math.floorMod(i + d, CHARS.length()));
    }

    public boolean done() {
        return done;
    }

    public String value() {
        return new String(name).trim().isEmpty() ? "???" : new String(name);
    }

    public void render(Graphics2D g, double cx, double y, double s) {
        double cell = 54 * s;
        double x0 = cx - cell * LENGTH / 2.0;
        for (int i = 0; i < LENGTH; i++) {
            double x = x0 + i * cell;
            boolean sel = i == cursor && !done;
            UiKit.panel(g, x + 4 * s, y, cell - 8 * s, cell * 1.2);
            Color c = sel && time % 0.5 < 0.3 ? UiKit.ACCENT : UiKit.TEXT;
            UiKit.outlinedText(g, String.valueOf(name[i]), x + cell / 2, y + cell * 0.95, UiKit.display((float) (44 * s)), c,
                    UiKit.Align.CENTER);
            if (sel) {
                g.setColor(UiKit.ACCENT);
                g.fillRect((int) (x + 10 * s), (int) (y + cell * 1.25), (int) (cell - 20 * s), (int) (4 * s));
            }
        }
    }
}
