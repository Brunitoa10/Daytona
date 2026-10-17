package com.daytona.render.theme;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Escalado suave, sin efectos. */
public final class SmoothPostProcessor implements PostProcessor {

    @Override
    public Object interpolation() {
        return RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    }

    @Override
    public void overlay(Graphics2D screen, int x, int y, int w, int h, int sourceHeight) {
        // sin efectos
    }
}
