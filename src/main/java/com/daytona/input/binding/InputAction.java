package com.daytona.input.binding;

/** Acciones de manejo que se pueden asignar a un eje o botón de un dispositivo. */
public enum InputAction {
    STEER("Dirección (girá a la DERECHA a tope)", true),
    THROTTLE("Acelerador (pisá/apretá a fondo)", true),
    BRAKE("Freno (pisá/apretá a fondo)", true),
    SHIFT_UP("Subir marcha", false),
    SHIFT_DOWN("Bajar marcha", false),
    GEAR_1("Palanca H: 1ra", false),
    GEAR_2("Palanca H: 2da", false),
    GEAR_3("Palanca H: 3ra", false),
    GEAR_4("Palanca H: 4ta", false),
    GEAR_5("Palanca H: 5ta", false),
    GEAR_6("Palanca H: 6ta", false),
    VIEW("Cambiar cámara", false),
    PAUSE("Pausa / Start", false);

    private final String prompt;
    private final boolean axis;

    InputAction(String prompt, boolean axis) {
        this.prompt = prompt;
        this.axis = axis;
    }

    public String prompt() {
        return prompt;
    }

    /** true si la acción espera un eje analógico (aunque acepta botón). */
    public boolean prefersAxis() {
        return axis;
    }

    public static InputAction gear(int gear) {
        switch (gear) {
            case 1:
                return GEAR_1;
            case 2:
                return GEAR_2;
            case 3:
                return GEAR_3;
            case 4:
                return GEAR_4;
            case 5:
                return GEAR_5;
            case 6:
                return GEAR_6;
            default:
                throw new IllegalArgumentException("marcha " + gear);
        }
    }
}
