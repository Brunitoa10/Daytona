package com.daytona.input;

import com.daytona.domain.car.DriveCommand;

import java.awt.event.KeyEvent;

/**
 * Teclado: flechas o WASD. La dirección digital se suaviza para simular un eje
 * analógico (girar más fino con toques cortos).
 * X / Shift derecho = subir, Z / Ctrl derecho = bajar, 1-6 = marcha directa, C = cámara.
 */
public final class KeyboardDriveSource implements DriveInputSource {

    private static final double STEER_IN = 4.5;
    private static final double STEER_OUT = 7.0;

    private final KeyboardState keys;
    private final double sensitivity;
    private double steer;

    public KeyboardDriveSource(KeyboardState keys, double sensitivity) {
        this.keys = keys;
        this.sensitivity = sensitivity;
    }

    @Override
    public DriveCommand read(double dt) {
        boolean left = keys.isDown(KeyEvent.VK_LEFT, KeyEvent.VK_A);
        boolean right = keys.isDown(KeyEvent.VK_RIGHT, KeyEvent.VK_D);
        double target = (right ? 1 : 0) - (left ? 1 : 0);
        double rate = (target == 0 || Math.signum(target) != Math.signum(steer)) ? STEER_OUT : STEER_IN * sensitivity;
        steer = approach(steer, target, rate * dt);

        double throttle = keys.isDown(KeyEvent.VK_UP, KeyEvent.VK_W) ? 1 : 0;
        double brake = keys.isDown(KeyEvent.VK_DOWN, KeyEvent.VK_S, KeyEvent.VK_SPACE) ? 1 : 0;
        boolean up = keys.pressed(KeyEvent.VK_X, KeyEvent.VK_E, KeyEvent.VK_SHIFT);
        boolean down = keys.pressed(KeyEvent.VK_Z, KeyEvent.VK_Q, KeyEvent.VK_CONTROL);
        int requested = -1;
        for (int g = 1; g <= 6; g++) {
            if (keys.pressed(KeyEvent.VK_0 + g) || keys.pressed(KeyEvent.VK_NUMPAD0 + g)) {
                requested = g;
            }
        }
        return new DriveCommand(steer, throttle, brake, up, down, -1, requested);
    }

    @Override
    public boolean viewPressed() {
        return keys.pressed(KeyEvent.VK_C, KeyEvent.VK_V);
    }

    @Override
    public boolean pausePressed() {
        return false; // la pausa de teclado se maneja como acción de menú (Esc/P)
    }

    @Override
    public String describe() {
        return "Teclado";
    }

    private static double approach(double v, double target, double step) {
        return v < target ? Math.min(target, v + step) : Math.max(target, v - step);
    }
}
