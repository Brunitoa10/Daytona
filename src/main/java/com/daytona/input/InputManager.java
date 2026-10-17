package com.daytona.input;

import com.daytona.core.Log;

import com.daytona.config.Settings;
import com.daytona.domain.car.DriveCommand;
import com.daytona.input.binding.BindingProfile;
import com.daytona.input.binding.BindingRepository;
import com.daytona.input.binding.DefaultProfiles;
import com.daytona.input.joystick.JoystickBackend;
import com.daytona.input.joystick.JoystickInfo;
import com.daytona.input.joystick.JoystickSnapshot;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fachada de entrada (patrón Facade): junta teclado y joysticks, ofrece acciones de
 * menú desde cualquier dispositivo y el manejo desde el modo de control elegido.
 */
public final class InputManager {

    private static final Log LOG = Log.of("input");
    private static final double REPEAT_DELAY = 0.38;
    private static final double REPEAT_RATE = 0.10;
    private static final float STICK_MENU = 0.6f;

    private final KeyboardState keyboard;
    private final JoystickBackend backend;
    private final BindingRepository bindings;
    private final Settings settings;
    private final Map<JoystickInfo, JoystickSnapshot> snapshots = new LinkedHashMap<>();
    private List<JoystickInfo> devices = java.util.Collections.emptyList();

    private final int actions = MenuAction.values().length;
    private final boolean[] raw = new boolean[actions];
    private final boolean[] fired = new boolean[actions];
    private final double[] heldTime = new double[actions];
    /** Acciones que siguen apretadas desde antes de un cambio de pantalla: se ignoran hasta soltarlas. */
    private final boolean[] suppressed = new boolean[actions];

    private DriveInputSource source;
    private JoystickInfo activeDevice;
    private boolean fallbackToKeyboard;

    public InputManager(KeyboardState keyboard, JoystickBackend backend, BindingRepository bindings, Settings settings) {
        this.keyboard = keyboard;
        this.backend = backend;
        this.bindings = bindings;
        this.settings = settings;
        this.source = new KeyboardDriveSource(keyboard, 1);
    }

    // ----------------------------------------------------------- dispositivos

    public void rescan() {
        try {
            devices = java.util.Collections.unmodifiableList(new ArrayList<>(backend.scan()));
        } catch (RuntimeException e) {
            LOG.warn("Error buscando joysticks: " + e);
            devices = java.util.Collections.emptyList();
        }
        snapshots.clear();
        for (JoystickInfo d : devices) {
            snapshots.put(d, new JoystickSnapshot());
        }
        refreshSource();
    }

    /** Reconstruye la fuente de manejo según las opciones actuales. */
    public void refreshSource() {
        ControlMode mode = settings.controlMode();
        double sens = settings.steerSensitivity() / 100.0;
        fallbackToKeyboard = false;
        activeDevice = null;
        if (mode == ControlMode.KEYBOARD) {
            source = new KeyboardDriveSource(keyboard, sens);
            return;
        }
        Optional<JoystickInfo> device = pickDevice(mode);
        if (!device.isPresent()) {
            fallbackToKeyboard = true;
            source = new KeyboardDriveSource(keyboard, sens);
            return;
        }
        activeDevice = device.get();
        BindingProfile profile = profileFor(activeDevice);
        source = new JoystickDriveSource(activeDevice, snapshots.get(activeDevice), profile,
                mode == ControlMode.WHEEL, settings.shifter(), sens, settings.deadzone() / 100.0);
    }

    public BindingProfile profileFor(JoystickInfo device) {
        Optional<BindingProfile> saved;
        try {
            saved = bindings.find(device.key());
        } catch (RuntimeException e) {
            saved = Optional.empty();
        }
        return saved.orElseGet(() -> DefaultProfiles.forDevice(device));
    }

    public void saveProfile(BindingProfile profile) {
        bindings.save(profile);
        refreshSource();
    }

    public void resetProfile(JoystickInfo device) {
        bindings.delete(device.key());
        refreshSource();
    }

    private Optional<JoystickInfo> pickDevice(ControlMode mode) {
        String wanted = settings.deviceKey();
        if (!wanted.isEmpty()) {
            for (JoystickInfo d : devices) {
                if (d.key().equals(wanted)) {
                    return Optional.of(d);
                }
            }
        }
        if (mode == ControlMode.GAMEPAD) {
            Optional<JoystickInfo> x = first(d -> d.source() == JoystickInfo.Source.XINPUT);
            if (x.isPresent()) {
                return x;
            }
            Optional<JoystickInfo> pad = first(d -> !d.likelyWheel());
            return pad.isPresent() ? pad : first(d -> true);
        }
        Optional<JoystickInfo> wheel = first(JoystickInfo::likelyWheel);
        if (wheel.isPresent()) {
            return wheel;
        }
        Optional<JoystickInfo> direct = first(d -> d.source() != JoystickInfo.Source.XINPUT);
        return direct.isPresent() ? direct : first(d -> true);
    }

    private Optional<JoystickInfo> first(java.util.function.Predicate<JoystickInfo> filter) {
        return devices.stream().filter(filter).findFirst();
    }

    // ----------------------------------------------------------- por paso

    public void poll(double dt) {
        keyboard.update();
        for (Map.Entry<JoystickInfo, JoystickSnapshot> e : snapshots.entrySet()) {
            backend.poll(e.getKey(), e.getValue());
        }
        updateMenu(dt);
    }

    private void updateMenu(double dt) {
        java.util.Arrays.fill(raw, false);
        set(MenuAction.UP, keyboard.isDown(KeyEvent.VK_UP, KeyEvent.VK_W));
        set(MenuAction.DOWN, keyboard.isDown(KeyEvent.VK_DOWN, KeyEvent.VK_S));
        set(MenuAction.LEFT, keyboard.isDown(KeyEvent.VK_LEFT, KeyEvent.VK_A));
        set(MenuAction.RIGHT, keyboard.isDown(KeyEvent.VK_RIGHT, KeyEvent.VK_D));
        set(MenuAction.CONFIRM, keyboard.isDown(KeyEvent.VK_ENTER));
        set(MenuAction.BACK, keyboard.isDown(KeyEvent.VK_ESCAPE, KeyEvent.VK_BACK_SPACE));
        set(MenuAction.PAUSE, keyboard.isDown(KeyEvent.VK_ESCAPE, KeyEvent.VK_P));
        for (Map.Entry<JoystickInfo, JoystickSnapshot> e : snapshots.entrySet()) {
            JoystickInfo d = e.getKey();
            JoystickSnapshot s = e.getValue();
            if (!s.connected()) {
                continue;
            }
            set(MenuAction.UP, s.button(JoystickSnapshot.HAT_UP));
            set(MenuAction.DOWN, s.button(JoystickSnapshot.HAT_DOWN));
            set(MenuAction.LEFT, s.button(JoystickSnapshot.HAT_LEFT));
            set(MenuAction.RIGHT, s.button(JoystickSnapshot.HAT_RIGHT));
            if (!d.likelyWheel()) {
                set(MenuAction.UP, s.axis(1) < -STICK_MENU);
                set(MenuAction.DOWN, s.axis(1) > STICK_MENU);
                set(MenuAction.LEFT, s.axis(0) < -STICK_MENU);
                set(MenuAction.RIGHT, s.axis(0) > STICK_MENU);
            }
            set(MenuAction.CONFIRM, s.button(DefaultProfiles.confirmButton(d)));
            set(MenuAction.BACK, s.button(DefaultProfiles.backButton(d)));
            set(MenuAction.PAUSE, s.button(DefaultProfiles.startButton(d)));
        }
        for (MenuAction a : MenuAction.values()) {
            int i = a.ordinal();
            boolean repeatable = a == MenuAction.UP || a == MenuAction.DOWN || a == MenuAction.LEFT || a == MenuAction.RIGHT;
            fired[i] = false;
            if (suppressed[i]) {
                suppressed[i] = raw[i];
                heldTime[i] = 0;
                continue;
            }
            if (raw[i]) {
                if (heldTime[i] == 0) {
                    fired[i] = true;
                } else if (repeatable && heldTime[i] > REPEAT_DELAY) {
                    double before = heldTime[i] - REPEAT_DELAY;
                    double after = before + dt;
                    fired[i] = Math.floor(after / REPEAT_RATE) > Math.floor(before / REPEAT_RATE);
                }
                heldTime[i] += dt;
            } else {
                heldTime[i] = 0;
            }
        }
    }

    private void set(MenuAction a, boolean v) {
        raw[a.ordinal()] |= v;
    }

    /** Acción de menú en este paso (incluye auto-repetición de direcciones). */
    public boolean pressed(MenuAction a) {
        return fired[a.ordinal()];
    }

    /** Evita que una tecla "rebote" a la pantalla siguiente. */
    public void consumeAll() {
        java.util.Arrays.fill(fired, false);
        for (int i = 0; i < actions; i++) {
            suppressed[i] = raw[i];
        }
    }

    public DriveCommand drive(double dt) {
        return source.read(dt);
    }

    public boolean viewPressed() {
        return source.viewPressed() || (!(source instanceof KeyboardDriveSource) && keyboard.pressed(KeyEvent.VK_C));
    }

    public boolean pausePressed() {
        return pressed(MenuAction.PAUSE) || source.pausePressed();
    }

    // ----------------------------------------------------------- consultas

    public KeyboardState keyboard() {
        return keyboard;
    }

    public List<JoystickInfo> devices() {
        return devices;
    }

    public List<JoystickInfo> devicesFor(ControlMode mode) {
        List<JoystickInfo> list = new ArrayList<>(devices);
        if (mode == ControlMode.WHEEL) {
            list.sort((a, b) -> Boolean.compare(b.likelyWheel(), a.likelyWheel()));
        }
        return list;
    }

    public Optional<JoystickInfo> activeDevice() {
        return Optional.ofNullable(activeDevice);
    }

    public JoystickSnapshot snapshot(JoystickInfo d) {
        return snapshots.getOrDefault(d, new JoystickSnapshot());
    }

    public String sourceDescription() {
        return fallbackToKeyboard ? "Teclado (no se detectó " + settings.controlMode().label().toLowerCase() + ")"
                : source.describe();
    }

    public boolean usingFallback() {
        return fallbackToKeyboard;
    }

    public String backendName() {
        return backend.name();
    }

    public void close() {
        backend.close();
    }
}
