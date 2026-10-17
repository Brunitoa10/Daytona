package com.daytona.input.joystick;

import java.nio.file.Paths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Linux: lee /dev/input/jsN (API joystick del kernel) con Java puro. */
final class LinuxJsBackend implements JoystickBackend {

    private static final int JS_EVENT_BUTTON = 0x01;
    private static final int JS_EVENT_AXIS = 0x02;
    private static final int JS_EVENT_INIT = 0x80;

    private final Map<Integer, Reader> readers = new ConcurrentHashMap<>();

    private static final class Reader implements Runnable {
        final JoystickSnapshot state = new JoystickSnapshot();
        final Path path;
        volatile boolean alive = true;

        Reader(Path path) {
            this.path = path;
        }

        @Override
        public void run() {
            byte[] ev = new byte[8];
            try (InputStream in = Files.newInputStream(path)) {
                while (alive) {
                    int read = readFully(in, ev);
                    if (read < 8) {
                        break;
                    }
                    short value = (short) ((ev[4] & 0xFF) | (ev[5] << 8));
                    int type = ev[6] & 0xFF & ~JS_EVENT_INIT;
                    int number = ev[7] & 0xFF;
                    synchronized (state) {
                        if (type == JS_EVENT_AXIS && number < JoystickSnapshot.MAX_AXES) {
                            state.axes[number] = Math.max(-1f, value / 32767f);
                        } else if (type == JS_EVENT_BUTTON && number < 32) {
                            state.buttons[number] = value != 0;
                        }
                        state.connected = true;
                    }
                }
            } catch (IOException ignored) {
                // desconectado
            }
            alive = false;
            synchronized (state) {
                state.connected = false;
            }
        }
    }

    private static int readFully(InputStream in, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int n = in.read(buf, total, buf.length - total);
            if (n < 0) {
                break;
            }
            total += n;
        }
        return total;
    }

    @Override
    public String name() {
        return "Linux joystick";
    }

    @Override
    public List<JoystickInfo> scan() {
        List<JoystickInfo> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Path p = Paths.get("/dev/input/js" + i);
            if (!Files.isReadable(p)) {
                continue;
            }
            String name = readName(i);
            readers.computeIfAbsent(i, k -> {
                Reader r = new Reader(p);
                Thread t = new Thread(r, "js" + k);
                t.setDaemon(true);
                t.start();
                return r;
            });
            list.add(new JoystickInfo("linux:" + name, name, JoystickInfo.Source.LINUX, i, 0, 0, 8, 32));
        }
        readers.entrySet().removeIf(e -> !e.getValue().alive);
        return list;
    }

    private static String readName(int i) {
        try {
            return new String(Files.readAllBytes(Paths.get("/sys/class/input/js" + i + "/device/name")), java.nio.charset.StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "Joystick " + (i + 1);
        }
    }

    @Override
    public boolean poll(JoystickInfo device, JoystickSnapshot out) {
        Reader r = readers.get(device.nativeId());
        if (r == null || !r.alive) {
            out.clear();
            return false;
        }
        synchronized (r.state) {
            out.copyFrom(r.state);
        }
        return true;
    }

    @Override
    public void close() {
        readers.values().forEach(r -> r.alive = false);
    }
}
