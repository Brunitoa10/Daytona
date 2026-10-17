package com.daytona.render.hud;

import com.daytona.domain.race.Race;
import com.daytona.domain.race.Racer;

import java.awt.Graphics2D;

/**
 * Apariencia de los indicadores de carrera (reloj, vuelta, posición, velocímetro).
 * Patrón Strategy: el {@link com.daytona.render.HudRenderer} no sabe cómo se dibujan.
 */
public interface HudSkin {

    /**
     * @param s     escala de interfaz (alto / 540)
     * @param clock segundos acumulados (para parpadeos)
     */
    public void drawPanels(Graphics2D g, int w, int h, double s, Race race, Racer player, double clock);
}
