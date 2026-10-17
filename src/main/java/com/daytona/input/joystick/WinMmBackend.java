package com.daytona.input.joystick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Joysticks DirectInput vía WinMM (joyGetPosEx): volantes, pedales, palancas H,
 * joysticks y gamepads genéricos. Hasta 6 ejes, 32 botones y un POV. Windows 7+.
 */
final class WinMmBackend implements JoystickBackend {

    private static final int MAX_DEVICES = 16;
    private static final int AXES = 6;
    private static final int POV_CENTERED = 0xFFFF;

    private final int[] caps = new int[16];
    private final char[] name = new char[32];
    private final int[] pos = new int[8];
    private final Map<Integer, int[]> ranges = new HashMap<>();

    WinMmBackend() {
        if (!NativeInput.load()) {
            throw new IllegalStateException("DLL de entrada no disponible");
        }
    }

    @Override
    public String name() {
        return "DirectInput (WinMM)";
    }

    @Override
    public synchronized List<JoystickInfo> scan() {
        List<JoystickInfo> list = new ArrayList<>();
        ranges.clear();
        for (int id = 0; id < MAX_DEVICES; id++) {
            if (NativeInput.joyCaps(id, caps, name) != 0 || NativeInput.joyPos(id, pos) != 0) {
                continue;
            }
            int[] rg = new int[AXES * 2];
            System.arraycopy(caps, 4, rg, 0, AXES * 2);
            ranges.put(id, rg);
            String n = readName();
            String key = String.format("winmm:%04X:%04X", caps[0] & 0xFFFF, caps[1] & 0xFFFF);
            list.add(new JoystickInfo(key, n.isEmpty() ? "Joystick " + (id + 1) : n, JoystickInfo.Source.WINMM, id,
                    caps[0] & 0xFFFF, caps[1] & 0xFFFF, caps[3], caps[2]));
        }
        return list;
    }

    private String readName() {
        StringBuilder sb = new StringBuilder();
        for (char c : name) {
            if (c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }

    @Override
    public synchronized boolean poll(JoystickInfo device, JoystickSnapshot out) {
        if (NativeInput.joyPos(device.nativeId(), pos) != 0) {
            out.clear();
            return false;
        }
        int[] rg = ranges.get(device.nativeId());
        for (int a = 0; a < AXES; a++) {
            double v = pos[a] & 0xFFFFFFFFL;
            double min = rg != null ? rg[a * 2] & 0xFFFFFFFFL : 0;
            double max = rg != null ? rg[a * 2 + 1] & 0xFFFFFFFFL : 65535;
            if (max <= min) {
                min = 0;
                max = 65535;
            }
            out.axes[a] = (float) (((v - min) / (max - min)) * 2 - 1);
        }
        int buttons = pos[6];
        for (int b = 0; b < 32; b++) {
            out.buttons[b] = (buttons & (1 << b)) != 0;
        }
        int pov = pos[7] & 0xFFFF;
        out.setHat(pov == POV_CENTERED ? -1 : pov / 100.0);
        out.connected = true;
        return true;
    }
}
