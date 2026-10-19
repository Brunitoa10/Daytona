package com.daytona;

import com.daytona.domain.car.Car;
import com.daytona.domain.car.CarCatalog;
import com.daytona.domain.car.CarPhysics;
import com.daytona.domain.car.CarSpec;
import com.daytona.domain.car.DriveCommand;
import com.daytona.domain.car.TransmissionType;
import com.daytona.domain.car.Transmissions;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackCatalog;
import com.daytona.domain.track.TrackConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarPhysicsTest {

    private static final double DT = 1.0 / 120;
    private final Track oval = new TrackCatalog().get("sunrise", false);
    private final CarSpec spec = new CarCatalog().byId("falcon");
    private final CarPhysics physics = new CarPhysics();

    private Car car(TransmissionType t) {
        Car c = new Car(spec, Transmissions.create(t, spec.gears()));
        c.place(0, 0);
        return c;
    }

    @Test
    public void automaticReachesNearTopSpeedAndShifts() {
        Car c = car(TransmissionType.AUTOMATIC);
        double timeTo200 = -1;
        for (int i = 0; i < 120 * 20; i++) {
            physics.update(c, DriveCommand.of(0, 1, 0), oval, DT, true);
            c.setX(0); // mantener en el centro para medir solo aceleración
            if (timeTo200 < 0 && TrackConstants.unitsToKmh(c.speed()) >= 200) {
                timeTo200 = i * DT;
            }
        }
        double kmh = TrackConstants.unitsToKmh(c.speed());
        assertTrue(kmh > spec.topSpeedKmh() * 0.9, "velocidad final " + kmh);
        assertEquals(4, c.gear());
        assertTrue(timeTo200 > 2 && timeTo200 < 9, "0-200 en " + timeTo200);
    }

    @Test
    public void manualNeutralDoesNotMove() {
        Car c = car(TransmissionType.MANUAL);
        for (int i = 0; i < 240; i++) {
            physics.update(c, new DriveCommand(0, 1, 0, false, false, 0, -1), oval, DT, true);
        }
        assertEquals(0, c.speed(), 1e-6);
        assertTrue(c.rpm() > 0.8, "debe acelerar en vacío");
    }

    @Test
    public void offRoadSlowsDown() {
        Track canyon = new TrackCatalog().get("canyon", false);
        Car c = car(TransmissionType.AUTOMATIC);
        c.place(0, 2.0);
        c.setSpeed(spec.topSpeed());
        for (int i = 0; i < 240; i++) {
            physics.update(c, DriveCommand.of(0, 1, 0), canyon, DT, true);
            c.setX(2.0);
        }
        assertTrue(c.speedRatio() < 0.45, "en la banquina no debería ir a fondo: " + c.speedRatio());
    }
}
