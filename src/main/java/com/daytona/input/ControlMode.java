package com.daytona.input;

/** Modo de control elegido por el jugador. */
public enum ControlMode {
    KEYBOARD("Teclado"),
    GAMEPAD("Joystick / Gamepad"),
    WHEEL("Volante + pedales + palanca");

    private final String label;

    private ControlMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
