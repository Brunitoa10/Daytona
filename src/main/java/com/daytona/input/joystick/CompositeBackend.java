package com.daytona.input.joystick;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Patrón Composite: combina XInput + DirectInput(WinMM) en Windows. */
public final class CompositeBackend implements JoystickBackend {

    private final List<JoystickBackend> backends;
    private final Map<JoystickInfo.Source, JoystickBackend> bySource = new java.util.EnumMap<>(JoystickInfo.Source.class);

    public CompositeBackend(List<JoystickBackend> backends) {
        this.backends = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(backends));
    }

    @Override
    public String name() {
        return backends.stream().map(JoystickBackend::name).collect(Collectors.joining(" + "));
    }

    @Override
    public List<JoystickInfo> scan() {
        List<JoystickInfo> all = new ArrayList<>();
        for (JoystickBackend b : backends) {
            for (JoystickInfo info : b.scan()) {
                bySource.put(info.source(), b);
                all.add(info);
            }
        }
        return all;
    }

    @Override
    public boolean poll(JoystickInfo device, JoystickSnapshot out) {
        JoystickBackend b = bySource.get(device.source());
        return b != null && b.poll(device, out);
    }

    @Override
    public void close() {
        backends.forEach(JoystickBackend::close);
    }
}
