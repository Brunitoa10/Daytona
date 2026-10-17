package com.daytona.input.joystick;

import java.util.List;

/** Abstracción del sistema operativo para leer joysticks (Dependency Inversion). */
public interface JoystickBackend extends AutoCloseable {

    String name();

    /** Busca dispositivos conectados (puede tardar; no llamar en cada frame). */
    List<JoystickInfo> scan();

    /** Lee el estado actual. Devuelve false si el dispositivo se desconectó. */
    boolean poll(JoystickInfo device, JoystickSnapshot out);

    @Override
    default void close() {
    }
}
