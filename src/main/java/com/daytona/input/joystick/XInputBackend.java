package com.daytona.input.joystick;

import java.util.ArrayList;
import java.util.List;

/**
 * Gamepads Xbox 360 / One / Series y compatibles vía XInput (xinput1_4 en Windows 8+,
 * xinput1_3 o xinput9_1_0 en Windows 7).
 * Ejes: 0 LX, 1 LY (abajo +), 2 RX, 3 RY, 4 gatillo izq (0..1), 5 gatillo der (0..1).
 * Botones: bits de wButtons, ver {@link XInputLayout}.
 */
public final class XInputBackend implements JoystickBackend {

    private static final int DPAD_UP = 0;
    private static final int DPAD_DOWN = 1;
    private static final int DPAD_LEFT = 2;
    private static final int DPAD_RIGHT = 3;

    private final int[] state = new int[7];

    public XInputBackend() {
        if (!NativeInput.load() || NativeInput.xinputVersion() == 0) {
            throw new IllegalStateException("XInput no disponible");
        }
    }

    @Override
    public String name() {
        return "XInput";
    }

    @Override
    public synchronized List<JoystickInfo> scan() {
        List<JoystickInfo> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (NativeInput.xinputState(i, state) == 0) {
                list.add(new JoystickInfo("xinput:gamepad", "Gamepad Xbox " + (i + 1),
                        JoystickInfo.Source.XINPUT, i, 0x045E, 0x028E, 6, 16));
            }
        }
        return list;
    }

    @Override
    public synchronized boolean poll(JoystickInfo device, JoystickSnapshot out) {
        if (NativeInput.xinputState(device.nativeId(), state) != 0) {
            out.clear();
            return false;
        }
        int buttons = state[0] & 0xFFFF;
        out.set(0, stick(state[3]));
        out.set(1, -stick(state[4]));
        out.set(2, stick(state[5]));
        out.set(3, -stick(state[6]));
        out.set(4, state[1] / 255f);
        out.set(5, state[2] / 255f);
        for (int b = 0; b < 16; b++) {
            out.press(b, (buttons & (1 << b)) != 0);
        }
        out.press(JoystickSnapshot.HAT_UP, out.button(DPAD_UP));
        out.press(JoystickSnapshot.HAT_DOWN, out.button(DPAD_DOWN));
        out.press(JoystickSnapshot.HAT_LEFT, out.button(DPAD_LEFT));
        out.press(JoystickSnapshot.HAT_RIGHT, out.button(DPAD_RIGHT));
        out.setConnected(true);
        return true;
    }

    private static float stick(int v) {
        return Math.max(-1f, (short) v / 32767f);
    }
}
