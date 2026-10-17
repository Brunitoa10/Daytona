package com.daytona.domain.race;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Tabla de posiciones: terminados por tiempo, el resto por distancia recorrida. */
public final class Standings {

    private final List<Racer> order;
    private final Comparator<Racer> comparator;

    public Standings(List<Racer> racers, double trackLength) {
        this.order = new ArrayList<>(racers);
        this.comparator = Comparator.<Racer>comparingInt(r -> r.finished() ? 0 : 1)
                .thenComparingDouble(r -> r.finished() ? r.finishTime() : -r.progress(trackLength));
        update();
    }

    public void update() {
        order.sort(comparator);
        for (int i = 0; i < order.size(); i++) {
            order.get(i).position = i + 1;
        }
    }

    public List<Racer> asList() {
        return Collections.unmodifiableList(order);
    }
}
