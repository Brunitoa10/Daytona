package com.daytona.domain.race;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/** Recorrido de una vuelta muestreado en el tiempo; se guarda comprimido en la base. */
public final class GhostData {

    private static final int VERSION = 1;

    private final float[] time;
    private final float[] z;
    private final float[] x;
    private final double lapTime;
    private final String carId;

    public GhostData(float[] time, float[] z, float[] x, double lapTime, String carId) {
        this.time = time;
        this.z = z;
        this.x = x;
        this.lapTime = lapTime;
        this.carId = carId;
    }

    public double lapTime() {
        return lapTime;
    }

    public String carId() {
        return carId;
    }

    public int size() {
        return time.length;
    }

    /**
     * Posición del fantasma a {@code t} segundos del inicio de la vuelta.
     *
     * @param out out[0] = z dentro de la vuelta, out[1] = x
     * @return false si la vuelta del fantasma ya terminó
     */
    public boolean sample(double t, double[] out) {
        if (time.length < 2 || t > time[time.length - 1]) {
            return false;
        }
        int lo = 0;
        int hi = time.length - 1;
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (time[mid] <= t) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        double span = time[hi] - time[lo];
        double p = span <= 0 ? 0 : Math.max(0, Math.min(1, (t - time[lo]) / span));
        out[0] = z[lo] + (z[hi] - z[lo]) * p;
        out[1] = x[lo] + (x[hi] - x[lo]) * p;
        return true;
    }

    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes))) {
            out.writeInt(VERSION);
            out.writeUTF(carId);
            out.writeDouble(lapTime);
            out.writeInt(time.length);
            for (int i = 0; i < time.length; i++) {
                out.writeFloat(time[i]);
                out.writeFloat(z[i]);
                out.writeFloat(x[i]);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    public static Optional<GhostData> fromBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return Optional.empty();
        }
        try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)))) {
            if (in.readInt() != VERSION) {
                return Optional.empty();
            }
            String car = in.readUTF();
            double lap = in.readDouble();
            int n = in.readInt();
            float[] t = new float[n];
            float[] z = new float[n];
            float[] x = new float[n];
            for (int i = 0; i < n; i++) {
                t[i] = in.readFloat();
                z[i] = in.readFloat();
                x[i] = in.readFloat();
            }
            return Optional.of(new GhostData(t, z, x, lap, car));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
