package com.daytona.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.function.IntSupplier;

/**
 * Administra la pantalla activa y las transiciones con fundido a negro.
 */
public final class ScreenManager {

    private static final double FADE_SECONDS = 0.25;

    private final IntSupplier width;
    private final IntSupplier height;
    private Screen current;
    private Screen pending;
    private double fade;          // 0 = sin fundido, 1 = negro total
    private boolean fadingOut;

    public ScreenManager(IntSupplier width, IntSupplier height) {
        this.width = width;
        this.height = height;
    }

    /** Cambia de pantalla con fundido. */
    public void show(Screen next) {
        if (current == null) {
            current = next;
            current.onEnter();
            fade = 1;
            fadingOut = false;
            return;
        }
        pending = next;
        fadingOut = true;
    }

    public Screen current() {
        return current;
    }

    public boolean isTransitioning() {
        return pending != null || fade > 0;
    }

    public void update(double dt) {
        if (fadingOut) {
            fade = Math.min(1, fade + dt / FADE_SECONDS);
            if (fade >= 1) {
                swap();
            }
            return; // no se procesa input durante el fundido de salida
        }
        if (fade > 0) {
            fade = Math.max(0, fade - dt / FADE_SECONDS);
        }
        if (current != null) {
            current.update(dt);
        }
    }

    private void swap() {
        if (current != null) {
            current.onExit();
        }
        current = pending;
        pending = null;
        fadingOut = false;
        if (current != null) {
            current.onEnter();
        }
    }

    public void render(Graphics2D g, double alpha) {
        if (current != null) {
            current.render(g, alpha);
        }
        if (fade > 0) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fade));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width.getAsInt(), height.getAsInt());
            g.setComposite(old);
        }
    }
}
