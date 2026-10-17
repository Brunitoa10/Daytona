package com.daytona.input.joystick;

import java.util.List;

/** Abstracción del sistema operativo para leer joysticks (Dependency Inversion). */
public interface JoystickBackend extends AutoCloseable {

    public String name();

    /** Busca dispositivos conectados (puede tardar; no llamar en cada frame). */
    public List<JoystickInfo> scan();

    /** Lee el estado actual. Devuelve false si el dispositivo se desconectó. */
    public boolean poll(JoystickInfo device, JoystickSnapshot out);

    @Override
    public default void close() {
    }
}
