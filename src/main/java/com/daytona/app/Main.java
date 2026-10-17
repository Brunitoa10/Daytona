package com.daytona.app;

import javax.swing.JOptionPane;

/** Punto de entrada. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        configureJava2D();
        try {
            new GameApp().run();
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                JOptionPane.showMessageDialog(null, "Error inesperado:\n" + t, "Daytona", JOptionPane.ERROR_MESSAGE);
            } catch (Throwable ignored) {
                // sin interfaz gráfica
            }
        }
        System.exit(0);
    }

    /** Ajustes del pipeline gráfico para máxima fluidez (antes de iniciar AWT). */
    private static void configureJava2D() {
        String os = System.getProperty("os.name", "").toLowerCase();
        setIfAbsent("sun.java2d.uiScale", "1.0");
        setIfAbsent("sun.awt.noerasebackground", "true");
        if (os.contains("win")) {
            setIfAbsent("sun.java2d.d3d", "true");
            setIfAbsent("sun.java2d.ddforcevram", "true");
            enableHighResolutionTimer();
        }
    }

    /**
     * En Windows (sobre todo 7) el reloj del sistema avanza de a ~15 ms. Un hilo dormido
     * "para siempre" hace que la JVM active el temporizador de 1 ms, imprescindible para
     * limitar FPS sin tirones.
     */
    private static void enableHighResolutionTimer() {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "timer-1ms");
        t.setDaemon(true);
        t.start();
    }

    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
