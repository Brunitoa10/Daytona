package com.daytona.app;

import com.daytona.config.Settings;
import com.daytona.core.Screen;
import com.daytona.core.ScreenManager;
import com.daytona.input.InputManager;
import com.daytona.screens.BindingWizardScreen;
import com.daytona.screens.RaceScreen;

import java.awt.event.KeyEvent;

/**
 * Bucle de juego con paso fijo de física (120 Hz) e interpolación de render:
 * la simulación es estable e independiente de los FPS y el movimiento se ve fluido
 * en monitores de 60, 144 o más Hz.
 */
public final class GameLoop {

    public static final double STEP = 1.0 / 120.0;
    private static final double MAX_FRAME = 0.25;
    private static final double RESCAN_SECONDS = 5;

    private final GameWindow window;
    private final ScreenManager screens;
    private final InputManager input;
    private final Settings settings;

    public GameLoop(GameWindow window, ScreenManager screens, InputManager input, Settings settings) {
        this.window = window;
        this.screens = screens;
        this.input = input;
        this.settings = settings;
    }

    public void run() {
        long last = System.nanoTime();
        double accumulator = 0;
        double fpsTimer = 0;
        int frames = 0;
        double rescanTimer = 0;
        long nextFrame = System.nanoTime();
        while (!window.quitRequested()) {
            long now = System.nanoTime();
            double frame = Math.min(MAX_FRAME, (now - last) / 1e9);
            last = now;
            accumulator += frame;
            while (accumulator >= STEP) {
                input.poll(STEP);
                handleGlobalKeys();
                screens.update(STEP);
                accumulator -= STEP;
            }
            double alpha = accumulator / STEP;
            window.present(g -> screens.render(g, alpha));

            frames++;
            fpsTimer += frame;
            if (fpsTimer >= 0.5) {
                window.setFps(frames / fpsTimer);
                frames = 0;
                fpsTimer = 0;
            }
            rescanTimer += frame;
            if (rescanTimer > RESCAN_SECONDS && allowsRescan(screens.current())) {
                rescanTimer = 0;
                input.rescan(); // detecta dispositivos enchufados en caliente
            }
            nextFrame = pace(nextFrame);
        }
    }

    private static boolean allowsRescan(Screen s) {
        return !(s instanceof RaceScreen) && !(s instanceof BindingWizardScreen);
    }

    private void handleGlobalKeys() {
        com.daytona.input.KeyboardState k = input.keyboard();
        if (k.pressed(KeyEvent.VK_F11) || (k.isDown(KeyEvent.VK_ALT) && k.pressed(KeyEvent.VK_ENTER))) {
            window.toggleFullscreen();
        }
        if (k.pressed(KeyEvent.VK_F3)) {
            settings.setShowFps(!settings.showFps());
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Limita los FPS durmiendo con precisión (sleep + espera activa corta). */
    private long pace(long nextFrame) {
        int limit = settings.fpsLimit();
        if (limit <= 0) {
            Thread.yield();
            return System.nanoTime();
        }
        long period = 1_000_000_000L / limit;
        long target = nextFrame + period;
        long now = System.nanoTime();
        if (target < now - period) {
            return now; // muy atrasado: no intentar recuperar
        }
        while (true) {
            long remaining = target - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            if (remaining > 2_000_000) {
                sleepQuietly(1);
            } else {
                Thread.yield();
            }
        }
        return target;
    }
}
