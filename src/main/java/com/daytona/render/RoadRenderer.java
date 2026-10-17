package com.daytona.render;

import com.daytona.domain.track.Track;
import com.daytona.render.car.CarLayer;
import com.daytona.render.car.CarRenderer;
import com.daytona.render.road.GroundLayer;
import com.daytona.render.road.RoadFrame;
import com.daytona.render.road.SceneryLayer;
import com.daytona.render.road.SegmentLayer;
import com.daytona.render.road.TunnelLayer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;

/**
 * Motor pseudo-3D: proyecta la pista ({@link RoadFrame}) y recorre los segmentos de
 * lejos a cerca (algoritmo del pintor) pidiéndole a cada {@link SegmentLayer} que
 * dibuje lo suyo. Así las colinas tapan autos y escenario de forma natural.
 */
public final class RoadRenderer {

    private final RoadFrame frame = new RoadFrame();
    private final CarLayer cars;
    private final List<SegmentLayer> layers;

    public RoadRenderer(SpriteAtlas atlas, CarRenderer carRenderer) {
        this.cars = new CarLayer(carRenderer);
        this.layers = Arrays.asList(new GroundLayer(), new TunnelLayer(), new SceneryLayer(atlas), cars);
    }

    public void setCarRenderer(CarRenderer renderer) {
        cars.setRenderer(renderer);
    }

    public void render(Graphics2D g, int w, int h, Track track, Camera cam, List<CarSprite> carSprites, int drawDistance) {
        frame.project(track, cam, w, h, drawDistance);
        cars.setCars(carSprites);
        for (SegmentLayer layer : layers) {
            layer.begin(frame);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Composite base = g.getComposite();
        for (int i = frame.count() - 1; i >= 1; i--) {
            if (!frame.valid(i)) {
                continue;
            }
            for (SegmentLayer layer : layers) {
                layer.paint(g, frame, i);
            }
            g.setComposite(base);
        }
    }

    /** Rectángulo en pantalla del auto del jugador en el último frame (para partículas). */
    public Rectangle playerRect() {
        return cars.playerRect();
    }

    public boolean playerVisible() {
        return cars.playerVisible();
    }
}
