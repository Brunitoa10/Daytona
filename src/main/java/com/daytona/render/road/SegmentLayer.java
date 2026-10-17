package com.daytona.render.road;

import java.awt.Graphics2D;

/**
 * Una capa del dibujo de la pista (piso, túneles, escenario, autos...). El
 * {@link com.daytona.render.RoadRenderer} recorre los segmentos de lejos a cerca y
 * le pide a cada capa que dibuje el suyo. Agregar un efecto nuevo = agregar una capa
 * (principio Abierto/Cerrado), sin tocar el motor.
 */
public interface SegmentLayer {

    /** Preparación antes de recorrer los segmentos (opcional). */
    public default void begin(RoadFrame frame) {
    }

    public void paint(Graphics2D g, RoadFrame frame, int segment);
}
