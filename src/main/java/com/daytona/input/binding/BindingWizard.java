package com.daytona.input.binding;

import com.daytona.input.joystick.JoystickSnapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Asistente que detecta automáticamente qué eje o botón mueve el jugador para
 * cada acción. Mide el valor en reposo y a fondo, así funciona con cualquier
 * volante, pedalera o palanca sin conocer la marca.
 */
public final class BindingWizard {

    public enum Stage { SETTLING, LISTENING, CAPTURED, DONE }

    private static final float AXIS_THRESHOLD = 0.45f;
    private static final double HOLD_SECONDS = 0.5;
    private static final double SETTLE_SECONDS = 0.6;
    private static final double CAPTURED_SECONDS = 0.55;

    private final String deviceKey;
    private final List<InputAction> steps;
    private final Map<InputAction, Binding> result = new EnumMap<>(InputAction.class);
    private final float[] baseline = new float[JoystickSnapshot.MAX_AXES];
    private final boolean[] baseButtons = new boolean[JoystickSnapshot.MAX_BUTTONS];

    private int step;
    private Stage stage = Stage.SETTLING;
    private double timer;
    private int candidateAxis = -1;
    private float candidateValue;
    private float lastValue;
    private double holdTimer;
    private String lastCaptured = "";

    public BindingWizard(String deviceKey, boolean includeHShifter) {
        this.deviceKey = deviceKey;
        List<InputAction> list = new ArrayList<>(java.util.Arrays.asList(InputAction.STEER, InputAction.THROTTLE, InputAction.BRAKE,
                InputAction.SHIFT_UP, InputAction.SHIFT_DOWN));
        if (includeHShifter) {
            for (int g = 1; g <= 6; g++) {
                list.add(InputAction.gear(g));
            }
        }
        list.add(InputAction.VIEW);
        list.add(InputAction.PAUSE);
        this.steps = java.util.Collections.unmodifiableList(list);
    }

    public void update(JoystickSnapshot s, double dt) {
        switch (stage) {
            case SETTLING:
                settle(s, dt);
                break;
            case LISTENING:
                listen(s, dt);
                break;
            case CAPTURED:
                timer += dt;
                if (timer > CAPTURED_SECONDS) {
                    next();
                }
                break;
            default:
                break;
        }
    }

    /** Espera a que el jugador suelte todo y toma la referencia de reposo. */
    private void settle(JoystickSnapshot s, double dt) {
        boolean stable = true;
        for (int a = 0; a < JoystickSnapshot.MAX_AXES; a++) {
            if (Math.abs(s.axis(a) - baseline[a]) > 0.03f) {
                stable = false;
            }
            baseline[a] = s.axis(a);
        }
        for (int b = 0; b < JoystickSnapshot.MAX_BUTTONS; b++) {
            baseButtons[b] = s.button(b);
        }
        timer = stable ? timer + dt : 0;
        if (timer >= SETTLE_SECONDS) {
            stage = Stage.LISTENING;
            timer = 0;
            candidateAxis = -1;
            holdTimer = 0;
        }
    }

    private void listen(JoystickSnapshot s, double dt) {
        InputAction action = current();
        for (int b = 0; b < JoystickSnapshot.MAX_BUTTONS; b++) {
            if (s.button(b) && !baseButtons[b]) {
                capture(action, Binding.button(b));
                return;
            }
        }
        int best = -1;
        float bestDelta = AXIS_THRESHOLD;
        for (int a = 0; a < JoystickSnapshot.MAX_AXES; a++) {
            float d = Math.abs(s.axis(a) - baseline[a]);
            if (d > bestDelta) {
                bestDelta = d;
                best = a;
            }
        }
        if (best < 0) {
            candidateAxis = -1;
            holdTimer = 0;
            return;
        }
        float v = s.axis(best);
        if (best != candidateAxis) {
            candidateAxis = best;
            candidateValue = v;
            lastValue = v;
            holdTimer = 0;
        }
        if (Math.abs(v - baseline[best]) > Math.abs(candidateValue - baseline[best])) {
            candidateValue = v;
        }
        holdTimer = Math.abs(v - lastValue) < 0.03f ? holdTimer + dt : 0;
        lastValue = v;
        if (holdTimer >= HOLD_SECONDS) {
            capture(action, Binding.axis(best, baseline[best], candidateValue));
        }
    }

    private void capture(InputAction action, Binding binding) {
        result.put(action, binding);
        lastCaptured = binding.describe();
        stage = Stage.CAPTURED;
        timer = 0;
    }

    public void skip() {
        if (stage != Stage.DONE) {
            next();
        }
    }

    private void next() {
        step++;
        timer = 0;
        stage = step >= steps.size() ? Stage.DONE : Stage.SETTLING;
    }

    public InputAction current() {
        return steps.get(Math.min(step, steps.size() - 1));
    }

    public Stage stage() {
        return stage;
    }

    public int stepNumber() {
        return Math.min(step + 1, steps.size());
    }

    public int totalSteps() {
        return steps.size();
    }

    public String lastCaptured() {
        return lastCaptured;
    }

    public double holdProgress() {
        return Math.min(1, holdTimer / HOLD_SECONDS);
    }

    public boolean isDone() {
        return stage == Stage.DONE;
    }

    public BindingProfile profile() {
        return new BindingProfile(deviceKey, result, true);
    }
}
