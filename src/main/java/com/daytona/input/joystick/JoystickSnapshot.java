package com.daytona.input.joystick;

import java.util.Arrays;

/** Estado instantáneo de un dispositivo: ejes normalizados -1..1 y botones. */
public final class JoystickSnapshot {

    public static final int MAX_AXES = 8;
    public static final int MAX_BUTTONS = 40;
    /** Índices virtuales del hat/POV como botones. */
    public static final int HAT_UP = 32;
    public static final int HAT_RIGHT = 33;
    public static final int HAT_DOWN = 34;
    public static final int HAT_LEFT = 35;

    private final float[] axes = new float[MAX_AXES];
    private final boolean[] buttons = new boolean[MAX_BUTTONS];
    private boolean connected;

    public float axis(int i) {
        return i >= 0 && i < MAX_AXES ? axes[i] : 0f;
    }

    public boolean button(int i) {
        return i >= 0 && i < MAX_BUTTONS && buttons[i];
    }

    public boolean connected() {
        return connected;
    }

    public void copyFrom(JoystickSnapshot o) {
        System.arraycopy(o.axes, 0, axes, 0, MAX_AXES);
        System.arraycopy(o.buttons, 0, buttons, 0, MAX_BUTTONS);
        connected = o.connected;
    }

    public void clear() {
        Arrays.fill(axes, 0f);
        Arrays.fill(buttons, false);
        connected = false;
    }

    public boolean anyButton() {
        for (boolean b : buttons) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    /** Convierte un POV (grados, negativo = centrado) en 4 botones virtuales. */
    public void setHat(double degrees) {
        boolean c = degrees < 0;
        buttons[HAT_UP] = !c && (degrees >= 292.5 || degrees <= 67.5);
        buttons[HAT_RIGHT] = !c && degrees >= 22.5 && degrees <= 157.5;
        buttons[HAT_DOWN] = !c && degrees >= 112.5 && degrees <= 247.5;
        buttons[HAT_LEFT] = !c && degrees >= 202.5 && degrees <= 337.5;
    }

    /** Para pruebas y backends en otros paquetes. */
    public void set(int axis, float value) {
        axes[axis] = value;
    }

    public void setConnected(boolean c) {
        this.connected = c;
    }

    public void press(int button, boolean down) {
        buttons[button] = down;
    }
}
