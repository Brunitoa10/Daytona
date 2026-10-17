package com.daytona.input.binding;

import com.daytona.input.joystick.JoystickInfo;
import com.daytona.input.joystick.JoystickSnapshot;
import com.daytona.input.joystick.XInputLayout;

import java.util.EnumMap;
import java.util.Map;

/**
 * Perfiles estimados para que el dispositivo funcione "enchufar y jugar".
 * Si algo no responde, el asistente de asignación crea un perfil a medida.
 */
public final class DefaultProfiles {

    private DefaultProfiles() {
    }

    public static BindingProfile forDevice(JoystickInfo device) {
        Map<InputAction, Binding> m = new EnumMap<>(InputAction.class);
        if (device.source() == JoystickInfo.Source.XINPUT) {
            xinput(m);
        } else if (device.likelyWheel()) {
            wheel(m, device);
        } else {
            genericPad(m);
        }
        return new BindingProfile(device.key(), m, false);
    }

    private static void xinput(Map<InputAction, Binding> m) {
        m.put(InputAction.STEER, Binding.axis(XInputLayout.AXIS_LX, 0, 1));
        m.put(InputAction.THROTTLE, Binding.axis(XInputLayout.AXIS_RT, 0, 1));
        m.put(InputAction.BRAKE, Binding.axis(XInputLayout.AXIS_LT, 0, 1));
        m.put(InputAction.SHIFT_UP, Binding.button(XInputLayout.RB));
        m.put(InputAction.SHIFT_DOWN, Binding.button(XInputLayout.LB));
        m.put(InputAction.VIEW, Binding.button(XInputLayout.Y));
        m.put(InputAction.PAUSE, Binding.button(XInputLayout.START));
    }

    /** Volantes Logitech G25/G27/G29/G923 y genéricos en DirectInput. */
    private static void wheel(Map<InputAction, Binding> m, JoystickInfo device) {
        // Volantes de 900°: dirección a fondo con ~135° de giro (sensación arcade)
        m.put(InputAction.STEER, Binding.axis(0, 0, 0.3f));
        if (device.isLogitechWheel()) {
            // Pedales separados: en reposo +1, a fondo -1
            m.put(InputAction.THROTTLE, Binding.axis(1, 1, -1));
            m.put(InputAction.BRAKE, Binding.axis(3, 1, -1));
            m.put(InputAction.SHIFT_UP, Binding.button(4));
            m.put(InputAction.SHIFT_DOWN, Binding.button(5));
            for (int g = 1; g <= 6; g++) {
                m.put(InputAction.gear(g), Binding.button(11 + g));
            }
            m.put(InputAction.VIEW, Binding.button(3));
            m.put(InputAction.PAUSE, Binding.button(9));
        } else {
            m.put(InputAction.THROTTLE, Binding.axis(1, 1, -1));
            m.put(InputAction.BRAKE, Binding.axis(2, 1, -1));
            m.put(InputAction.SHIFT_UP, Binding.button(0));
            m.put(InputAction.SHIFT_DOWN, Binding.button(1));
            m.put(InputAction.VIEW, Binding.button(2));
            m.put(InputAction.PAUSE, Binding.button(9));
        }
    }

    private static void genericPad(Map<InputAction, Binding> m) {
        m.put(InputAction.STEER, Binding.axis(0, 0, 1));
        m.put(InputAction.THROTTLE, Binding.button(1));
        m.put(InputAction.BRAKE, Binding.button(0));
        m.put(InputAction.SHIFT_UP, Binding.button(5));
        m.put(InputAction.SHIFT_DOWN, Binding.button(4));
        m.put(InputAction.VIEW, Binding.button(3));
        m.put(InputAction.PAUSE, Binding.button(9));
    }

    /** Botón de confirmación en menús según el tipo de dispositivo. */
    public static int confirmButton(JoystickInfo d) {
        return d.source() == JoystickInfo.Source.XINPUT ? XInputLayout.A : 0;
    }

    public static int backButton(JoystickInfo d) {
        return d.source() == JoystickInfo.Source.XINPUT ? XInputLayout.B : 1;
    }

    public static int startButton(JoystickInfo d) {
        return d.source() == JoystickInfo.Source.XINPUT ? XInputLayout.START : 9;
    }

    public static boolean hatUsable(JoystickSnapshot s) {
        return s.connected();
    }
}
