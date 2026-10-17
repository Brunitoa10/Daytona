package com.daytona.input.joystick;

import com.daytona.core.Log;

import java.util.ArrayList;
import java.util.List;

/** Factory: elige los backends según el sistema operativo (Windows 7+ vía JNI, Linux nativo en Java). */
public final class JoystickBackends {

    private static final Log LOG = Log.of("input");

    private JoystickBackends() {
    }

    public static JoystickBackend create() {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<JoystickBackend> list = new ArrayList<>();
        if (os.contains("win")) {
            tryAdd(list, "XInput", XInputBackend::new);
            tryAdd(list, "WinMM", WinMmBackend::new);
        } else if (os.contains("linux")) {
            tryAdd(list, "Linux js", LinuxJsBackend::new);
        }
        if (list.isEmpty()) {
            return new NullBackend();
        }
        return list.size() == 1 ? list.get(0) : new CompositeBackend(list);
    }

    private interface Factory {
        JoystickBackend create() throws Throwable;
    }

    private static void tryAdd(List<JoystickBackend> list, String name, Factory f) {
        try {
            list.add(f.create());
        } catch (Throwable t) {
            LOG.warn("Backend " + name + " no disponible: " + t);
        }
    }
}
