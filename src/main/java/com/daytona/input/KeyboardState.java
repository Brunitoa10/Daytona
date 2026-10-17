package com.daytona.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Estado del teclado. AWT entrega eventos en su hilo; el juego lo consulta desde el
 * bucle principal, por eso se usan arreglos volátiles y una cola concurrente.
 */
public final class KeyboardState extends KeyAdapter {

    private static final int SIZE = 1024;

    private final boolean[] down = new boolean[SIZE];
    /** Recuerda toques muy cortos (presionar y soltar entre dos pasos lógicos). */
    private final boolean[] latched = new boolean[SIZE];
    private final boolean[] previous = new boolean[SIZE];
    private final boolean[] current = new boolean[SIZE];
    private final ConcurrentLinkedQueue<Character> typed = new ConcurrentLinkedQueue<>();

    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (c >= 0 && c < SIZE) {
            synchronized (down) {
                down[c] = true;
                latched[c] = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int c = e.getKeyCode();
        if (c >= 0 && c < SIZE) {
            synchronized (down) {
                down[c] = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char ch = e.getKeyChar();
        if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == '.' || ch == '-') {
            typed.add(Character.toUpperCase(ch));
        }
    }

    /** Toma una "foto" del teclado para este paso lógico. */
    public void update() {
        System.arraycopy(current, 0, previous, 0, SIZE);
        synchronized (down) {
            for (int i = 0; i < SIZE; i++) {
                current[i] = down[i] || latched[i];
                latched[i] = false;
            }
        }
    }

    public boolean isDown(int keyCode) {
        return current[keyCode];
    }

    public boolean isDown(int... keyCodes) {
        for (int k : keyCodes) {
            if (current[k]) {
                return true;
            }
        }
        return false;
    }

    public boolean pressed(int keyCode) {
        return current[keyCode] && !previous[keyCode];
    }

    public boolean pressed(int... keyCodes) {
        for (int k : keyCodes) {
            if (pressed(k)) {
                return true;
            }
        }
        return false;
    }

    public Character pollTyped() {
        return typed.poll();
    }

    public void clearTyped() {
        typed.clear();
    }

    public void releaseAll() {
        synchronized (down) {
            java.util.Arrays.fill(down, false);
        }
    }
}
