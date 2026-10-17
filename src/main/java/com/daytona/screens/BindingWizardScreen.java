package com.daytona.screens;

import com.daytona.core.Screen;
import com.daytona.input.binding.BindingWizard;
import com.daytona.input.ControlMode;
import com.daytona.input.ShifterType;
import com.daytona.input.joystick.JoystickInfo;
import com.daytona.render.UiKit;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Asistente de asignación: pide mover cada control y detecta solo el eje o botón.
 * Se maneja con el teclado (ENTER/ESPACIO omitir, ESC cancelar) para no confundir
 * los botones del dispositivo que se está configurando.
 */
public final class BindingWizardScreen implements Screen {

    private final GameContext ctx;
    private final JoystickInfo device;
    private final Screen back;
    private final BindingWizard wizard;
    private double time;
    private double doneTimer = -1;

    public BindingWizardScreen(GameContext ctx, JoystickInfo device, Screen back) {
        this.ctx = ctx;
        this.device = device;
        this.back = back;
        boolean hShifter = ctx.settings().shifter() == ShifterType.H_PATTERN || ctx.settings().controlMode() == ControlMode.WHEEL;
        this.wizard = new BindingWizard(device.key(), hShifter);
    }

    @Override
    public void update(double dt) {
        time += dt;
        com.daytona.input.KeyboardState keys = ctx.input().keyboard();
        if (doneTimer >= 0) {
            doneTimer += dt;
            if (doneTimer > 1.5) {
                ctx.nav().back(back);
            }
            return;
        }
        if (keys.pressed(KeyEvent.VK_ESCAPE)) {
            ctx.nav().back(back);
            return;
        }
        if (keys.pressed(KeyEvent.VK_ENTER) || keys.pressed(KeyEvent.VK_SPACE)) {
            wizard.skip();
        }
        BindingWizard.Stage before = wizard.stage();
        wizard.update(ctx.input().snapshot(device), dt);
        if (before == BindingWizard.Stage.LISTENING && wizard.stage() == BindingWizard.Stage.CAPTURED) {
            ctx.audio().beep(990, 0.08);
        }
        if (wizard.isDone()) {
            ctx.input().saveProfile(wizard.profile());
            ctx.audio().beep(1320, 0.2);
            doneTimer = 0;
        }
    }

    @Override
    public void render(Graphics2D g, double alpha) {
        int w = ctx.display().width();
        int h = ctx.display().height();
        double s = UiKit.scale(h);
        UiKit.menuBackground(g, w, h, time);
        UiKit.smooth(g);
        UiKit.outlinedText(g, "ASIGNAR CONTROLES", w / 2.0, 60 * s, UiKit.display((float) (38 * s)), UiKit.ACCENT, UiKit.Align.CENTER);
        UiKit.text(g, device.label(), w / 2.0, 88 * s, UiKit.bodyBold((float) (15 * s)), UiKit.TEXT, UiKit.Align.CENTER);
        UiKit.panel(g, w * 0.18, 120 * s, w * 0.64, 250 * s);
        if (doneTimer >= 0) {
            UiKit.outlinedText(g, "¡PERFIL GUARDADO!", w / 2.0, 250 * s, UiKit.display((float) (34 * s)), UiKit.GOOD, UiKit.Align.CENTER);
            return;
        }
        UiKit.text(g, "Paso " + wizard.stepNumber() + " de " + wizard.totalSteps(), w / 2.0, 150 * s,
                UiKit.bodyBold((float) (14 * s)), UiKit.MUTED, UiKit.Align.CENTER);
        UiKit.outlinedText(g, wizard.current().prompt(), w / 2.0, 205 * s, UiKit.display((float) (26 * s)), UiKit.TEXT,
                UiKit.Align.CENTER);
        String hint;
        switch (wizard.stage()) {
            case SETTLING:
                hint = "Soltá todos los controles…";
                break;
            case LISTENING:
                hint = wizard.current().prefersAxis()
                    ? "Mové el control y mantenelo a fondo un instante" : "Apretá el botón o enganchá la marcha";
                break;
            case CAPTURED:
                hint = "Detectado: " + wizard.lastCaptured();
                break;
            case DONE:
                hint = "";
                break;
            default:
                throw new IllegalStateException(String.valueOf(wizard.stage()));
        }
        UiKit.text(g, hint, w / 2.0, 250 * s, UiKit.bodyBold((float) (16 * s)),
                wizard.stage() == BindingWizard.Stage.CAPTURED ? UiKit.GOOD : UiKit.ACCENT, UiKit.Align.CENTER);
        if (wizard.stage() == BindingWizard.Stage.LISTENING) {
            UiKit.bar(g, w * 0.3, 275 * s, w * 0.4, 10 * s, wizard.holdProgress(), UiKit.ACCENT);
        }
        UiKit.text(g, "ENTER / ESPACIO = omitir este paso      ESC = cancelar", w / 2.0, 345 * s,
                UiKit.body((float) (13 * s)), UiKit.MUTED, UiKit.Align.CENTER);
        UiKit.text(g, "Pedales y volantes de cualquier marca: se mide el reposo y el recorrido completo.",
                w / 2.0, h - 20 * s, UiKit.body((float) (12 * s)), UiKit.MUTED, UiKit.Align.CENTER);
    }
}
