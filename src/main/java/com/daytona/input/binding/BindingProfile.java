package com.daytona.input.binding;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Conjunto de asignaciones para un dispositivo concreto. */
public final class BindingProfile {

    private final String deviceKey;
    private final Map<InputAction, Binding> bindings;
    private final boolean custom;

    public BindingProfile(String deviceKey, Map<InputAction, Binding> bindings, boolean custom) {
        this.deviceKey = deviceKey;
        this.bindings = bindings.isEmpty() ? new EnumMap<>(InputAction.class) : new EnumMap<>(bindings);
        this.custom = custom;
    }

    public String deviceKey() {
        return deviceKey;
    }

    public Optional<Binding> get(InputAction action) {
        return Optional.ofNullable(bindings.get(action));
    }

    public Map<InputAction, Binding> asMap() {
        return Collections.unmodifiableMap(bindings);
    }

    /** true si el usuario lo configuró con el asistente (no es un perfil estimado). */
    public boolean isCustom() {
        return custom;
    }

    public boolean hasHShifter() {
        return bindings.containsKey(InputAction.GEAR_1);
    }
}
