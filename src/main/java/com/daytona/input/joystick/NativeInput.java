package com.daytona.input.joystick;

import com.daytona.core.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Puente JNI a {@code daytona_input_x86/x64.dll} (WinMM + XInput). La DLL viaja dentro
 * del .jar y se extrae a una carpeta temporal; funciona en Windows 7, 8, 10 y 11 con Java 8+.
 */
final class NativeInput {

    private static final Log LOG = Log.of("input");
    private static Boolean loaded;

    private NativeInput() {
    }

    /** Carga la DLL una sola vez. Devuelve false si no se pudo (el juego sigue con teclado). */
    static synchronized boolean load() {
        if (loaded != null) {
            return loaded;
        }
        String arch = System.getProperty("os.arch", "").contains("64") ? "x64" : "x86";
        String name = "daytona_input_" + arch + ".dll";
        try {
            File dll = findNextToJar(name);
            if (dll == null) {
                dll = extract(name);
            }
            System.load(dll.getAbsolutePath());
            loaded = Boolean.TRUE;
        } catch (IOException | UnsatisfiedLinkError | SecurityException e) {
            LOG.warn("No se pudo cargar " + name + ": " + e);
            loaded = Boolean.FALSE;
        }
        return loaded;
    }

    /** Versión portable: lib/native/ junto al .jar. */
    private static File findNextToJar(String name) {
        try {
            File jar = new File(NativeInput.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = jar.isFile() ? jar.getParentFile() : jar;
            File candidate = new File(dir, "lib" + File.separator + "native" + File.separator + name);
            return candidate.isFile() ? candidate : null;
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
    }

    private static File extract(String name) throws IOException {
        try (InputStream in = NativeInput.class.getResourceAsStream("/native/" + name)) {
            if (in == null) {
                throw new IOException("recurso /native/" + name + " no encontrado");
            }
            File tmp = File.createTempFile("daytona_input_", ".dll");
            tmp.deleteOnExit();
            Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }

    /** 0 = XInput no disponible; 14, 13 o 910 según la DLL encontrada. */
    static native int xinputVersion();

    /** out: botones, gatillo izq, gatillo der, LX, LY, RX, RY. Devuelve 0 si está conectado. */
    static native int xinputState(int index, int[] out);

    /** caps: vid, pid, botones, ejes y 6 pares min/max (X, Y, Z, R, U, V). Devuelve 0 si existe. */
    static native int joyCaps(int id, int[] caps, char[] name);

    /** out: X, Y, Z, R, U, V, botones (bits), POV (centésimas de grado, 65535 = centrado). 0 = OK. */
    static native int joyPos(int id, int[] out);
}
