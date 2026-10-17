package com.daytona.screens;

import com.daytona.core.Screen;
import com.daytona.domain.car.DriveCommand;
import com.daytona.domain.car.TransmissionType;
import com.daytona.input.ControlMode;
import com.daytona.input.ShifterType;
import com.daytona.input.joystick.JoystickInfo;
import com.daytona.input.joystick.JoystickSnapshot;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

/**
 * Configuración de controles: modo (teclado / gamepad / volante), dispositivo,
 * caja, palanca, sensibilidad, zona muerta y asistente de asignación, con un
 * monitor en vivo de ejes y botones para comprobar volante, pedales y palanca.
 */
public final class ControlsScreen extends MenuScreen {

    private final Screen back;
    private DriveCommand preview = DriveCommand.IDLE;
    private int lastGearShown = 1;
    private String status = "";

    public ControlsScreen(GameContext ctx, Screen back) {
        super(ctx);
        this.back = back;
    }

    @Override
    protected String title() {
        return "CONTROLES";
    }

    @Override
    protected double listWidth() {
        return 0.44;
    }

    @Override
    protected void buildItems(MenuList list) {
        com.daytona.config.Settings s = ctx.settings();
        list.add(MenuList.choice("Modo de control", () -> s.controlMode().label(), d -> {
            ControlMode[] v = ControlMode.values();
            s.setControlMode(v[Math.floorMod(s.controlMode().ordinal() + d, v.length)]);
            s.setDeviceKey("");
            ctx.input().refreshSource();
        }));
        list.add(MenuList.choice("Dispositivo", this::deviceLabel, this::cycleDevice,
                () -> s.controlMode() != ControlMode.KEYBOARD));
        list.add(MenuList.choice("Transmisión", () -> s.transmission().label(), d -> {
            TransmissionType[] v = TransmissionType.values();
            s.setTransmission(v[Math.floorMod(s.transmission().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Palanca de cambios", () -> s.shifter().label(), d -> {
            ShifterType[] v = ShifterType.values();
            s.setShifter(v[Math.floorMod(s.shifter().ordinal() + d, v.length)]);
            ctx.input().refreshSource();
        }));
        list.add(MenuList.choice("Sensibilidad dirección", () -> s.steerSensitivity() + " %", d -> {
            s.setSteerSensitivity(s.steerSensitivity() + d * 10);
            ctx.input().refreshSource();
        }));
        list.add(MenuList.choice("Zona muerta", () -> s.deadzone() + " %", d -> {
            s.setDeadzone(s.deadzone() + d);
            ctx.input().refreshSource();
        }, () -> s.controlMode() != ControlMode.KEYBOARD));
        list.add(MenuList.action("Asignar ejes y botones (asistente)", () -> {
            Optional<JoystickInfo> dev = ctx.input().activeDevice();
            if (dev.isPresent()) {
                ctx.nav().wizard(dev.get(), this);
            } else {
                status = "No hay joystick/volante activo";
            }
        }, () -> ctx.input().activeDevice().isPresent()));
        list.add(MenuList.action("Restaurar perfil estimado", () -> ctx.input().activeDevice().ifPresent(dev -> {
            ctx.input().resetProfile(dev);
            status = "Perfil restaurado";
        }), () -> ctx.input().activeDevice().isPresent()));
        list.add(MenuList.action("Buscar dispositivos", () -> {
            ctx.input().rescan();
            status = ctx.input().devices().size() + " dispositivo(s) encontrado(s)";
        }));
        list.add(MenuList.action("Volver", this::onBack));
    }

    private String deviceLabel() {
        if (ctx.settings().controlMode() == ControlMode.KEYBOARD) {
            return "—";
        }
        Optional<JoystickInfo> d = ctx.input().activeDevice();
        String name = d.map(JoystickInfo::label).orElse("ninguno");
        if (name.length() > 28) {
            name = name.substring(0, 27) + "…";
        }
        return (ctx.settings().deviceKey().isEmpty() ? "Auto: " : "") + name;
    }

    private void cycleDevice(int d) {
        List<JoystickInfo> devices = ctx.input().devicesFor(ctx.settings().controlMode());
        if (devices.isEmpty()) {
            status = "No se encontraron dispositivos. Conectalo y elegí 'Buscar dispositivos'.";
            return;
        }
        String current = ctx.settings().deviceKey();
        int idx = -1;
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).key().equals(current)) {
                idx = i;
            }
        }
        int next = Math.floorMod(idx + 1 + d, devices.size() + 1) - 1; // -1 = automático
        ctx.settings().setDeviceKey(next < 0 ? "" : devices.get(next).key());
        ctx.input().refreshSource();
    }

    @Override
    protected void onBack() {
        ctx.nav().back(back);
    }

    @Override
    public void update(double dt) {
        preview = ctx.input().drive(dt);
        if (preview.requestedGear() > 0) {
            lastGearShown = preview.requestedGear();
        } else if (preview.heldGear() >= 0) {
            lastGearShown = preview.heldGear();
        } else if (preview.shiftUp()) {
            lastGearShown = Math.min(6, lastGearShown + 1);
        } else if (preview.shiftDown()) {
            lastGearShown = Math.max(1, lastGearShown - 1);
        }
        super.update(dt);
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double s) {
        double px = w * 0.55;
        double pw = w * 0.4;
        UiKit.panel(g, px, 86 * s, pw, h - 130 * s);
        double x = px + 18 * s;
        double y = 116 * s;
        UiKit.shadowText(g, "PRUEBA EN VIVO", x, y, UiKit.display((float) (20 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
        y += 22 * s;
        UiKit.text(g, ctx.input().sourceDescription(), x, y, UiKit.body((float) (12 * s)),
                ctx.input().usingFallback() ? UiKit.BAD : UiKit.TEXT, UiKit.Align.LEFT);
        y += 26 * s;
        double bw = pw - 36 * s;
        UiKit.text(g, "Dirección", x, y, UiKit.bodyBold((float) (12 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        g.setColor(new Color(255, 255, 255, 35));
        g.fillRect((int) x, (int) (y + 6 * s), (int) bw, (int) (12 * s));
        double mid = x + bw / 2;
        g.setColor(UiKit.ACCENT);
        double sv = preview.steer() * bw / 2;
        g.fillRect((int) Math.min(mid, mid + sv), (int) (y + 6 * s), (int) Math.abs(sv) + 1, (int) (12 * s));
        y += 34 * s;
        UiKit.text(g, "Acelerador", x, y, UiKit.bodyBold((float) (12 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.bar(g, x, y + 6 * s, bw, 12 * s, preview.throttle(), UiKit.GOOD);
        y += 34 * s;
        UiKit.text(g, "Freno", x, y, UiKit.bodyBold((float) (12 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        UiKit.bar(g, x, y + 6 * s, bw, 12 * s, preview.brake(), UiKit.BAD);
        y += 40 * s;
        String gear = preview.heldGear() == 0 ? "N" : Integer.toString(lastGearShown);
        UiKit.text(g, "Marcha (prueba): " + gear, x, y, UiKit.bodyBold((float) (14 * s)), UiKit.ACCENT, UiKit.Align.LEFT);
        y += 16 * s;

        Optional<JoystickInfo> dev = ctx.input().activeDevice();
        if (dev.isPresent()) {
            JoystickSnapshot snap = ctx.input().snapshot(dev.get());
            y += 8 * s;
            UiKit.text(g, "Ejes crudos", x, y, UiKit.bodyBold((float) (12 * s)), UiKit.MUTED, UiKit.Align.LEFT);
            y += 6 * s;
            double aw = (bw - 7 * 6 * s) / 8;
            for (int a = 0; a < 8; a++) {
                double ax = x + a * (aw + 6 * s);
                g.setColor(new Color(255, 255, 255, 35));
                g.fillRect((int) ax, (int) y, (int) aw, (int) (40 * s));
                double v = (snap.axis(a) + 1) / 2;
                g.setColor(UiKit.ACCENT);
                g.fillRect((int) ax, (int) (y + 40 * s * (1 - v)), (int) aw, (int) (40 * s * v));
                UiKit.text(g, Integer.toString(a + 1), ax + aw / 2, y + 54 * s, UiKit.body((float) (10 * s)), UiKit.MUTED,
                        UiKit.Align.CENTER);
            }
            y += 70 * s;
            UiKit.text(g, "Botones", x, y, UiKit.bodyBold((float) (12 * s)), UiKit.MUTED, UiKit.Align.LEFT);
            y += 6 * s;
            double cell = bw / 18;
            for (int b = 0; b < 36; b++) {
                double bx = x + (b % 18) * cell;
                double by = y + (b / 18) * cell * 1.1;
                g.setColor(snap.button(b) ? UiKit.ACCENT : new Color(255, 255, 255, 35));
                g.fillRoundRect((int) bx, (int) by, (int) (cell * 0.8), (int) (cell * 0.8), 4, 4);
            }
            y += cell * 2.4;
        } else {
            y += 10 * s;
            String[] help = {
                    "←→ / A D: dirección",
                    "↑ / W: acelerar     ↓ / S / Espacio: frenar",
                    "X / E / Shift: subir marcha     Z / Q / Ctrl: bajar",
                    "1-6: marcha directa (caja manual)",
                    "C: cámara     Esc / P: pausa",
                    "F11 / Alt+Enter: pantalla completa     F3: FPS",
                    "Derrape: toque de freno + volantazo en curva"};
            for (String line : help) {
                UiKit.text(g, line, x, y, UiKit.body((float) (12 * s)), UiKit.TEXT, UiKit.Align.LEFT);
                y += 20 * s;
            }
        }
        UiKit.text(g, status, x, h - 56 * s, UiKit.bodyBold((float) (12 * s)), UiKit.GOOD, UiKit.Align.LEFT);
        UiKit.text(g, "Backend: " + ctx.input().backendName(), x, h - 56 * s + 18 * s, UiKit.body((float) (11 * s)), UiKit.MUTED,
                UiKit.Align.LEFT);
    }
}
