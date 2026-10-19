package com.daytona;

import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.race.Difficulty;
import com.daytona.domain.race.GameModeType;
import com.daytona.domain.race.GridBuilder;
import com.daytona.domain.race.RaceSetup;
import com.daytona.domain.race.Racer;
import com.daytona.domain.track.TrackCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GridBuilderTest {

    @Test
    public void fortyCarGridHasUniqueNamesAndOnePlayer() {
        CarCatalog cars = new CarCatalog();
        RaceSetup setup = new RaceSetup(new TrackCatalog().get("sunrise", false), cars.byId("falcon"),
                TransmissionType.AUTOMATIC, GameModeType.ARCADE.create(), 3, 39, Difficulty.NORMAL, "YOU",
                null, null, cars, 5);
        GridBuilder.Grid grid = GridBuilder.build(setup);
        assertEquals(40, grid.racers().size());
        assertNotNull(grid.player());
        Set<String> names = new HashSet<>();
        for (Racer r : grid.racers()) {
            names.add(r.name());
        }
        assertEquals(40, names.size(), "los nombres deben ser únicos para el campeonato");
    }
}
