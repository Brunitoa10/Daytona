package com.daytona.render.car;

import com.daytona.render.CarSprite;
import com.daytona.render.road.RoadFrame;
import com.daytona.render.road.SegmentLayer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Capa de autos: los reparte por segmento (de lejos a cerca) y delega el dibujo en
 * un {@link CarRenderer} intercambiable.
 */
public final class CarLayer implements SegmentLayer {

    private static final class Placed {
        final CarSprite car;
        final double percent;

        Placed(CarSprite car, double percent) {
            this.car = car;
            this.percent = percent;
        }
    }

    private final List<List<Placed>> buckets = new ArrayList<>();
    private final Rectangle tmp = new Rectangle();
    private final Rectangle playerRect = new Rectangle();
    private CarRenderer renderer;
    private List<CarSprite> cars = Collections.emptyList();
    private boolean playerVisible;

    public CarLayer(CarRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRenderer(CarRenderer renderer) {
        this.renderer = renderer;
    }

    public void setCars(List<CarSprite> cars) {
        this.cars = cars;
    }

    @Override
    public void begin(RoadFrame f) {
        int n = f.count();
        while (buckets.size() < n) {
            buckets.add(new ArrayList<Placed>(4));
        }
        for (int i = 0; i < n; i++) {
            buckets.get(i).clear();
        }
        playerVisible = false;
        double base = f.basePct() * RoadFrame.SEG;
        for (CarSprite c : cars) {
            double rel = f.relativeZ(c.z()) + base;
            int idx = (int) Math.floor(rel / RoadFrame.SEG);
            if (idx >= 1 && idx < n) {
                List<Placed> b = buckets.get(idx);
                double pct = (rel - idx * RoadFrame.SEG) / RoadFrame.SEG;
                int pos = 0;
                while (pos < b.size() && b.get(pos).percent > pct) {
                    pos++;
                }
                b.add(pos, new Placed(c, pct)); // los más lejanos primero
            }
        }
    }

    @Override
    public void paint(Graphics2D g, RoadFrame f, int i) {
        for (Placed p : buckets.get(i)) {
            if (renderer.draw(g, f, i, p.percent, p.car, tmp) && p.car.player()) {
                playerRect.setBounds(tmp);
                playerVisible = true;
            }
        }
    }

    public Rectangle playerRect() {
        return playerRect;
    }

    public boolean playerVisible() {
        return playerVisible;
    }
}
