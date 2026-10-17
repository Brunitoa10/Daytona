package com.daytona.input.binding;

import com.daytona.input.joystick.JoystickSnapshot;

/**
 * Asignación de una acción a un eje o botón.
 * <p>
 * Para ejes se guardan el valor en reposo y el valor a fondo, medidos por el asistente.
 * Así se soportan pedales invertidos, ejes combinados y volantes con cualquier rango
 * sin código especial por marca.
 */
public final class Binding {
    private final Kind kind;
    private final int index;
    private final float rest;
    private final float full;

    public Binding(Kind kind, int index, float rest, float full) {
        this.kind = kind;
        this.index = index;
        this.rest = rest;
        this.full = full;
    }

    public Kind kind() {
        return kind;
    }

    public int index() {
        return index;
    }

    public float rest() {
        return rest;
    }

    public float full() {
        return full;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Binding)) {
            return false;
        }
        Binding other = (Binding) o;
        return java.util.Objects.equals(kind, other.kind) && java.util.Objects.equals(index, other.index) && java.util.Objects.equals(rest, other.rest) && java.util.Objects.equals(full, other.full);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(kind, index, rest, full);
    }
    public enum Kind { AXIS, BUTTON }

    public static Binding axis(int index, float rest, float full) {
        return new Binding(Kind.AXIS, index, rest, full);
    }

    public static Binding button(int index) {
        return new Binding(Kind.BUTTON, index, 0, 1);
    }

    /** Valor 0..1 (pedales, botones). */
    public float readPositive(JoystickSnapshot s) {
        if (kind == Kind.BUTTON) {
            return s.button(index) ? 1f : 0f;
        }
        float range = full - rest;
        if (Math.abs(range) < 1e-4f) {
            return 0f;
        }
        return clamp((s.axis(index) - rest) / range, 0f, 1f);
    }

    /** Valor -1..1 respecto del centro (dirección). */
    public float readSigned(JoystickSnapshot s) {
        if (kind == Kind.BUTTON) {
            return s.button(index) ? 1f : 0f;
        }
        float range = full - rest;
        if (Math.abs(range) < 1e-4f) {
            return 0f;
        }
        return clamp((s.axis(index) - rest) / range, -1f, 1f);
    }

    public boolean pressed(JoystickSnapshot s) {
        return readPositive(s) > 0.5f;
    }

    public String describe() {
        return kind == Kind.BUTTON ? "Botón " + (index + 1) : "Eje " + (index + 1);
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
