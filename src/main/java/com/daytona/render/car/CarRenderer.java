package com.daytona.render.car;

import com.daytona.render.CarSprite;
import com.daytona.render.road.RoadFrame;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Patrón Strategy: cómo se dibuja un auto (polígonos 3D estilo arcade 1994 o sprite 2D).
 */
public interface CarRenderer {

    /**
     * @param segment índice del segmento visible donde está el auto
     * @param percent posición dentro del segmento (0..1)
     * @param bounds  se completa con el rectángulo ocupado en pantalla
     * @return true si se dibujó
     */
    public boolean draw(Graphics2D g, RoadFrame frame, int segment, double percent, CarSprite car, Rectangle bounds);
}
