package com.daytona;

import com.daytona.domain.race.GhostData;
import com.daytona.input.binding.Binding;
import com.daytona.input.binding.BindingWizard;
import com.daytona.input.binding.InputAction;
import com.daytona.input.joystick.JoystickSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputTest {

    @Test
    void invertedPedalIsNormalized() {
        JoystickSnapshot s = new JoystickSnapshot();
        Binding pedal = Binding.axis(1, 1f, -1f); // reposo +1, a fondo -1
        s.set(1, 1f);
        assertEquals(0f, pedal.readPositive(s), 1e-6);
        s.set(1, 0f);
        assertEquals(0.5f, pedal.readPositive(s), 1e-6);
        s.set(1, -1f);
        assertEquals(1f, pedal.readPositive(s), 1e-6);
    }

    @Test
    void wizardDetectsAxisAndButton() {
        BindingWizard w = new BindingWizard("dev", false);
        JoystickSnapshot s = new JoystickSnapshot();
        double dt = 1.0 / 120;
        for (int i = 0; i < 120; i++) {
            w.update(s, dt); // reposo
        }
        s.set(3, 0.95f); // gira a la derecha en el eje 4
        for (int i = 0; i < 120 && w.current() == InputAction.STEER; i++) {
            w.update(s, dt);
        }
        Binding steer = w.profile().get(InputAction.STEER).get();
        assertEquals(Binding.Kind.AXIS, steer.kind());
        assertEquals(3, steer.index());
        s.set(3, 0f);
        for (int i = 0; i < 200; i++) {
            w.update(s, dt);
        }
        w.skip(); // acelerador
        while (w.current() != InputAction.SHIFT_UP) {
            for (int i = 0; i < 100; i++) {
                w.update(s, dt);
            }
            if (w.current() == InputAction.BRAKE) {
                w.skip();
            }
        }
        for (int i = 0; i < 100; i++) {
            w.update(s, dt);
        }
        s.press(7, true);
        w.update(s, dt);
        assertEquals(7, w.profile().get(InputAction.SHIFT_UP).get().index());
    }

    @Test
    void ghostSerialization() {
        GhostData g = new GhostData(new float[]{0, 1, 2}, new float[]{0, 100, 300}, new float[]{0, 0.5f, -0.5f}, 2.0, "falcon");
        GhostData back = GhostData.fromBytes(g.toBytes()).get();
        double[] out = new double[2];
        assertTrue(back.sample(1.5, out));
        assertEquals(200, out[0], 1e-3);
        assertEquals(0, out[1], 1e-3);
        assertEquals("falcon", back.carId());
    }
}
