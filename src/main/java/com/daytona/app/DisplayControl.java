package com.daytona.app;

/** Lo que las pantallas pueden pedirle a la ventana (sin conocer Swing). */
public interface DisplayControl {

    /** Aplica resolución interna y pantalla completa desde las opciones. */
    public void applyVideoSettings();

    public void toggleFullscreen();

    public void requestQuit();

    public int width();

    public int height();

    public double fps();
}
