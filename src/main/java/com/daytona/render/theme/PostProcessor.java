package com.daytona.render.theme;

import java.awt.Graphics2D;

/** Cómo se lleva la imagen interna a la ventana (escalado y efectos de pantalla). */
public interface PostProcessor {

    /** Valor de {@link java.awt.RenderingHints#KEY_INTERPOLATION} para el escalado. */
    public Object interpolation();

    /** Efecto dibujado sobre la imagen ya escalada (x, y, w, h = área ocupada en la ventana). */
    public void overlay(Graphics2D screen, int x, int y, int w, int h, int sourceHeight);
}
