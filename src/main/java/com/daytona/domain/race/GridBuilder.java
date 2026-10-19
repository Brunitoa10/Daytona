package com.daytona.domain.race;

import com.daytona.domain.car.Car;
import com.daytona.domain.car.CarSpec;
import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.car.Transmissions;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Arma la grilla de largada (de a dos en fondo) con el jugador en el lugar que
 * indica el modo y los rivales con habilidad decreciente hacia el fondo.
 * Separado de {@link Race} para que la carrera solo se ocupe de correrla (SRP).
 */
public final class GridBuilder {

    private static final double GRID_SPACING = TrackConstants.SEGMENT_LENGTH * 3.5;

    /** Resultado: participantes ordenados por grilla y quién es el jugador. */
    public static final class Grid {
        private final List<Racer> racers;
        private final Racer player;

        private Grid(List<Racer> racers, Racer player) {
            this.racers = Collections.unmodifiableList(racers);
            this.player = player;
        }

        public List<Racer> racers() {
            return racers;
        }

        public Racer player() {
            return player;
        }
    }

    private GridBuilder() {
    }

    public static Grid build(RaceSetup setup) {
        Track track = setup.track();
        int rivals = setup.rivals();
        int total = rivals + 1;
        int playerSlot = (int) Math.round(setup.mode().gridSlot() * (total - 1));
        Random random = new Random(setup.seed());
        double baseSkill = setup.difficulty().aiSkill() + setup.mode().aiSkillBonus();
        List<Racer> racers = new ArrayList<>(total);
        Racer player = null;
        int rivalIndex = 0;
        for (int slot = 0; slot < total; slot++) {
            Racer racer;
            if (slot == playerSlot) {
                racer = createPlayer(setup, baseSkill, random);
                player = racer;
            } else {
                double lead = total > 1 ? 1 - slot / (double) (total - 1) : 1;
                double skill = baseSkill + 0.035 * lead - 0.015 + random.nextDouble() * 0.02;
                racer = createRival(setup, rivalIndex++, skill, random);
            }
            place(racer, track, slot, rivals == 0);
            racers.add(racer);
        }
        return new Grid(racers, player);
    }

    private static Racer createPlayer(RaceSetup setup, double baseSkill, Random random) {
        CarSpec spec = setup.playerCar();
        Car car = new Car(spec, Transmissions.create(setup.transmission(), spec.gears()));
        boolean human = setup.playerDriver() != null;
        Driver driver = human ? setup.playerDriver() : new AiDriver(baseSkill, 0, random.nextLong());
        return new Racer(setup.playerName(), car, driver, human);
    }

    private static Racer createRival(RaceSetup setup, int index, double skill, Random random) {
        CarSpec spec = setup.catalog().rival(index, setup.seed());
        Car car = new Car(spec, Transmissions.create(TransmissionType.AUTOMATIC, spec.gears()));
        return new Racer(setup.catalog().rivalName(index), car,
                new AiDriver(skill, setup.mode().rubberBand(), random.nextLong()), false);
    }

    private static void place(Racer racer, Track track, int slot, boolean alone) {
        if (alone) {
            racer.car().place(track.length() - TrackConstants.SEGMENT_LENGTH, 0); // contrarreloj
            return;
        }
        int row = slot / 2;
        double x = (slot % 2 == 0) ? -0.45 : 0.45;
        racer.car().place(track.length() - (row + 1) * GRID_SPACING, x);
    }
}
