package com.daytona.input.joystick;

import java.util.List;

/** Patrón Null Object: sistema sin soporte de joystick. */
public final class NullBackend implements JoystickBackend {

    @Override
    public String name() {
        return "sin joystick";
    }

    @Override
    public List<JoystickInfo> scan() {
        return java.util.Collections.emptyList();
    }

    @Override
    public boolean poll(JoystickInfo device, JoystickSnapshot out) {
        out.clear();
        return false;
    }
}
