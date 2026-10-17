package com.daytona.core;

import java.awt.Graphics2D;

/**
 * Patrón State: cada pantalla del juego (menú, carrera, resultados...) es un estado
 * intercambiable que el {@link ScreenManager} activa.
 */
public interface Screen {

    /** Se llama al volverse la pantalla activa. */
    default void onEnter() { }

    /** Se llama al dejar de ser la pantalla activa. */
    default void onExit() { }

    /** Lógica con paso fijo (segundos). */
    void update(double dt);

    /**
     * Dibuja la pantalla.
     *
     * @param alpha fracción [0,1) entre el último y el próximo paso lógico, para interpolar.
     */
    void render(Graphics2D g, double alpha);
}
