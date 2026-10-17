package com.daytona.input;

import com.daytona.domain.car.DriveCommand;
import com.daytona.input.binding.Binding;
import com.daytona.input.binding.BindingProfile;
import com.daytona.input.binding.InputAction;
import com.daytona.input.joystick.JoystickInfo;
import com.daytona.input.joystick.JoystickSnapshot;

import java.util.Optional;

/**
 * Gamepad o volante según un {@link BindingProfile}. Aplica zona muerta,
 * sensibilidad, curva exponencial (solo gamepads) y lectura de palanca H.
 */
public final class JoystickDriveSource implements DriveInputSource {

    private final JoystickInfo device;
    private final JoystickSnapshot snapshot;
    private final BindingProfile profile;
    private final boolean wheel;
    private final boolean hShifter;
    private final double sensitivity;
    private final double deadzone;
    private final boolean[] previous = new boolean[InputAction.values().length];
    private final boolean[] current = new boolean[InputAction.values().length];

    public JoystickDriveSource(JoystickInfo device, JoystickSnapshot snapshot, BindingProfile profile,
                               boolean wheel, ShifterType shifter, double sensitivity, double deadzone) {
        this.device = device;
        this.snapshot = snapshot;
        this.profile = profile;
        this.wheel = wheel;
        this.hShifter = shifter == ShifterType.H_PATTERN && profile.hasHShifter();
        this.sensitivity = sensitivity;
        this.deadzone = deadzone;
    }

    @Override
    public DriveCommand read(double dt) {
        for (InputAction a : InputAction.values()) {
            previous[a.ordinal()] = current[a.ordinal()];
            current[a.ordinal()] = profile.get(a).map(b -> b.pressed(snapshot)).orElse(false);
        }
        double steer = profile.get(InputAction.STEER).map(b -> (double) b.readSigned(snapshot)).orElse(0.0);
        steer = shapeSteer(steer);
        double throttle = pedal(profile.get(InputAction.THROTTLE));
        double brake = pedal(profile.get(InputAction.BRAKE));
        int held = -1;
        if (hShifter) {
            held = 0;
            for (int g = 1; g <= 6; g++) {
                if (current[InputAction.gear(g).ordinal()]) {
                    held = g;
                }
            }
        }
        return new DriveCommand(steer, throttle, brake, edge(InputAction.SHIFT_UP), edge(InputAction.SHIFT_DOWN), held, -1);
    }

    private double shapeSteer(double s) {
        double a = Math.abs(s);
        if (a < deadzone) {
            return 0;
        }
        a = (a - deadzone) / (1 - deadzone);
        if (!wheel) {
            a = Math.pow(a, 1.6); // más precisión cerca del centro en sticks
        }
        return Math.copySign(Math.min(1, a * sensitivity), s);
    }

    private double pedal(Optional<Binding> b) {
        double v = b.map(x -> (double) x.readPositive(snapshot)).orElse(0.0);
        return v < deadzone ? 0 : Math.min(1, (v - deadzone) / (1 - deadzone) * 1.05);
    }

    private boolean edge(InputAction a) {
        return current[a.ordinal()] && !previous[a.ordinal()];
    }

    @Override
    public boolean viewPressed() {
        return edge(InputAction.VIEW);
    }

    @Override
    public boolean pausePressed() {
        return edge(InputAction.PAUSE);
    }

    @Override
    public String describe() {
        return device.label() + (profile.isCustom() ? " (perfil propio)" : " (perfil estimado)");
    }
}
