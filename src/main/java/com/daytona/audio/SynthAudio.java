package com.daytona.audio;

import com.daytona.core.Log;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Sintetizador en tiempo real: todo el sonido se genera por código (motor, gomas,
 * choques, pitidos), así el juego no necesita archivos de audio y pesa poco.
 */
public final class SynthAudio implements AudioService, Runnable {

    private static final float RATE = 44100f;
    private static final int CHUNK = 512;

    private final SourceDataLine line;
    private final Thread thread;
    private final Random random = new Random(1);
    private final ConcurrentLinkedQueue<Voice> incoming = new ConcurrentLinkedQueue<>();
    private final List<Voice> oneShots = new ArrayList<>();
    private volatile boolean running = true;

    // parámetros del motor (escritos por el juego, leídos por el hilo de audio)
    private volatile double engineRpm;
    private volatile double engineThrottle;
    private volatile boolean engineOn;
    private volatile double skidTarget;
    private volatile double scrapeTarget;
    private volatile double volume = 0.7;

    // estado interno del hilo de audio
    private double enginePhase;
    private double engineFreq = 40;
    private double engineGain;
    private double engineLp;
    private double skidGain;
    private double skidLp1;
    private double skidLp2;
    private double scrapeGain;
    private double scrapeLp;

    private interface Voice {
        /** @return muestra o NaN si terminó */
        public double next();
    }

    private SynthAudio(SourceDataLine line) {
        this.line = line;
        this.thread = new Thread(this, "audio");
        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY - 1);
        thread.start();
    }

    /** Intenta abrir la salida de audio; si no hay, devuelve un servicio silencioso. */
    public static AudioService open() {
        try {
            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
            line.open(fmt, (int) (RATE * 2 * 0.07));
            line.start();
            return new SynthAudio(line);
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            Log.of("audio").warn("Sin audio: " + e.getMessage());
            return new SilentAudio();
        }
    }

    @Override
    public void run() {
        byte[] buf = new byte[CHUNK * 2];
        while (running) {
            Voice v;
            while ((v = incoming.poll()) != null) {
                oneShots.add(v);
            }
            for (int i = 0; i < CHUNK; i++) {
                double s = engine() + skid() + scrape();
                for (Iterator<Voice> it = oneShots.iterator(); it.hasNext(); ) {
                    double o = it.next().next();
                    if (Double.isNaN(o)) {
                        it.remove();
                    } else {
                        s += o;
                    }
                }
                s = Math.tanh(s * volume * 1.2); // limitador suave
                int pcm = (int) (s * 30000);
                buf[i * 2] = (byte) pcm;
                buf[i * 2 + 1] = (byte) (pcm >> 8);
            }
            line.write(buf, 0, buf.length);
        }
        line.drain();
        line.close();
    }

    private double engine() {
        double targetGain = engineOn ? 0.16 + 0.22 * engineThrottle : 0;
        engineGain += (targetGain - engineGain) * 0.002;
        if (engineGain < 1e-4) {
            return 0;
        }
        double targetFreq = 36 + engineRpm * 175;
        engineFreq += (targetFreq - engineFreq) * 0.0025;
        enginePhase += engineFreq / RATE;
        if (enginePhase > 1e6) {
            enginePhase -= 1e6;
        }
        double p = enginePhase - Math.floor(enginePhase);
        double saw = 2 * p - 1;
        double sub = (enginePhase * 0.5 - Math.floor(enginePhase * 0.5)) < 0.5 ? 0.6 : -0.6;
        double firing = 0.65 + 0.35 * Math.sin(enginePhase * Math.PI * 2 * 0.5);
        double raw = (saw * 0.7 + sub * 0.5) * firing + (random.nextDouble() - 0.5) * 0.25 * engineThrottle;
        double a = 0.08 + 0.25 * engineThrottle + 0.15 * engineRpm;
        engineLp += a * (raw - engineLp);
        return engineLp * engineGain;
    }

    private double skid() {
        skidGain += (skidTarget * 0.22 - skidGain) * 0.003;
        if (skidGain < 1e-4) {
            return 0;
        }
        double n = random.nextDouble() * 2 - 1;
        skidLp1 += 0.35 * (n - skidLp1);
        skidLp2 += 0.08 * (skidLp1 - skidLp2);
        return (skidLp1 - skidLp2) * skidGain * 2.2;
    }

    private double scrape() {
        scrapeGain += (scrapeTarget * 0.3 - scrapeGain) * 0.004;
        if (scrapeGain < 1e-4) {
            return 0;
        }
        double n = random.nextDouble() * 2 - 1;
        double crackle = random.nextDouble() < 0.02 ? (random.nextDouble() - 0.5) * 3 : 0;
        scrapeLp += 0.5 * (n - scrapeLp);
        return (scrapeLp + crackle) * scrapeGain;
    }

    @Override
    public void setEngine(double rpm, double throttle, boolean running) {
        engineRpm = Math.max(0, Math.min(1.1, rpm));
        engineThrottle = Math.max(0, Math.min(1, throttle));
        engineOn = running;
    }

    @Override
    public void setSkid(double level) {
        skidTarget = Math.max(0, Math.min(1, level));
    }

    @Override
    public void setScrape(double level) {
        scrapeTarget = Math.max(0, Math.min(1, level));
    }

    @Override
    public void beep(double frequency, double seconds, double delay) {
        int total = (int) (seconds * RATE);
        int wait = (int) (delay * RATE);
        incoming.add(new Voice() {
            int n;
            int waited;
            double phase;

            @Override
            public double next() {
                if (waited < wait) {
                    waited++;
                    return 0;
                }
                if (n >= total) {
                    return Double.NaN;
                }
                double env = Math.min(1, n / 200.0) * Math.min(1, (total - n) / 600.0);
                phase += frequency / RATE;
                n++;
                double sq = (phase - Math.floor(phase)) < 0.5 ? 1 : -1;
                return (Math.sin(phase * Math.PI * 2) * 0.6 + sq * 0.15) * env * 0.35;
            }
        });
    }

    @Override
    public void crash(double intensity) {
        int total = (int) (RATE * (0.25 + 0.45 * intensity));
        double amp = 0.3 + 0.6 * Math.max(0, Math.min(1, intensity));
        incoming.add(new Voice() {
            int n;
            double lp;

            @Override
            public double next() {
                if (n >= total) {
                    return Double.NaN;
                }
                double t = n / (double) total;
                n++;
                double noise = random.nextDouble() * 2 - 1;
                lp += (0.15 + 0.5 * (1 - t)) * (noise - lp);
                double thump = Math.sin(n / RATE * Math.PI * 2 * 55) * Math.exp(-t * 8);
                return (lp * Math.exp(-t * 5) + thump * 0.8) * amp;
            }
        });
    }

    @Override
    public void setVolume(double v) {
        volume = Math.max(0, Math.min(1, v));
    }

    @Override
    public void close() {
        running = false;
        try {
            thread.join(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
