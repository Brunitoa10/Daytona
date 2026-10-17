package com.daytona.app;

/** Lo que las pantallas pueden pedirle a la ventana (sin conocer Swing). */
public interface DisplayControl {

    /** Aplica resolución interna y pantalla completa desde las opciones. */
    void applyVideoSettings();

    void toggleFullscreen();

    void requestQuit();

    int width();

    int height();

    double fps();
}
