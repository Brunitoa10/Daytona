package com.daytona.domain.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Patrón Builder: construye la pista tramo a tramo con un mini-DSL
 * (rectas, curvas, colinas, túneles, objetos) y al final calcula
 * checkpoints, carteles de curva y el minimapa.
 */
public final class TrackBuilder {

    public static final int SHORT = 25;
    public static final int MEDIUM = 50;
    public static final int LONG = 100;

    public static final double EASY = 2;
    public static final double MID = 4;
    public static final double HARD = 6;

    private final boolean mirror;
    private final Random random;
    private final List<double[]> raw = new ArrayList<>(); // curve, y1, y2, tunnel, walls
    private final List<PendingSprite> pendingSprites = new ArrayList<>();
    private boolean tunnel;
    private boolean walls;
    private int checkpointsPerLap = 2;
    private boolean autoChevrons = true;

    private static final class PendingSprite {
        private final int index;
        private final SpriteKind kind;
        private final double offset;
        private final int variant;

        private PendingSprite(int index, SpriteKind kind, double offset, int variant) {
            this.index = index;
            this.kind = kind;
            this.offset = offset;
            this.variant = variant;
        }

        public int index() {
            return index;
        }

        public SpriteKind kind() {
            return kind;
        }

        public double offset() {
            return offset;
        }

        public int variant() {
            return variant;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PendingSprite)) {
                return false;
            }
            PendingSprite other = (PendingSprite) o;
            return java.util.Objects.equals(index, other.index) && java.util.Objects.equals(kind, other.kind) && java.util.Objects.equals(offset, other.offset) && java.util.Objects.equals(variant, other.variant);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(index, kind, offset, variant);
        }
    }

    public TrackBuilder(boolean mirror, long seed) {
        this.mirror = mirror;
        this.random = new Random(seed);
    }

    public int size() {
        return raw.size();
    }

    public Random random() {
        return random;
    }

    // ---------- trazado ----------

    /** Tramo genérico con entrada, mantenimiento y salida suavizadas. */
    public TrackBuilder road(int enter, int hold, int leave, double curve, double hillSegments) {
        double startY = lastY();
        double endY = startY + hillSegments * TrackConstants.SEGMENT_LENGTH;
        int total = enter + hold + leave;
        double c = mirror ? -curve : curve;
        for (int n = 0; n < enter; n++) {
            add(easeIn(0, c, n / (double) enter), easeInOut(startY, endY, (double) n / total));
        }
        for (int n = 0; n < hold; n++) {
            add(c, easeInOut(startY, endY, (double) (enter + n) / total));
        }
        for (int n = 0; n < leave; n++) {
            add(easeInOut(c, 0, n / (double) leave), easeInOut(startY, endY, (double) (enter + hold + n) / total));
        }
        return this;
    }

    public TrackBuilder straight(int n) {
        return road(n / 3, n - 2 * (n / 3), n / 3, 0, 0);
    }

    public TrackBuilder curve(int n, double curve) {
        return road(n / 3, n - 2 * (n / 3), n / 3, curve, 0);
    }

    public TrackBuilder curveHill(int n, double curve, double hill) {
        return road(n / 3, n - 2 * (n / 3), n / 3, curve, hill);
    }

    public TrackBuilder hill(int n, double height) {
        return road(n / 3, n - 2 * (n / 3), n / 3, 0, height);
    }

    public TrackBuilder sCurves(int each, double curve) {
        road(each, each, each, -curve, 0);
        road(each, each, each, curve, 0);
        road(each, each, each, curve, 0);
        road(each, each, each, -curve, 0);
        return this;
    }

    public TrackBuilder rollingHills(int each, double height) {
        road(each, each, each, 0, height / 2);
        road(each, each, each, 0, -height);
        road(each, each, each, 0, height);
        road(each, each, each, 0, -height / 2);
        return this;
    }

    public TrackBuilder tunnel(boolean on) {
        this.tunnel = on;
        return this;
    }

    public TrackBuilder walls(boolean on) {
        this.walls = on;
        return this;
    }

    public TrackBuilder checkpointsPerLap(int n) {
        this.checkpointsPerLap = n;
        return this;
    }

    public TrackBuilder autoChevrons(boolean on) {
        this.autoChevrons = on;
        return this;
    }

    // ---------- objetos ----------

    public TrackBuilder sprite(int index, SpriteKind kind, double offset) {
        return sprite(index, kind, offset, 0);
    }

    public TrackBuilder sprite(int index, SpriteKind kind, double offset, int variant) {
        pendingSprites.add(new PendingSprite(index, kind, offset, variant));
        return this;
    }

    /** Coloca objetos cada {@code step} segmentos entre from y to, con un leve desvío aleatorio. */
    public TrackBuilder every(int from, int to, int step, SpriteKind kind, double offset, double jitter) {
        for (int i = from; i < to; i += step) {
            double o = offset + (jitter == 0 ? 0 : (random.nextDouble() * jitter * Math.signum(offset)));
            sprite(i, kind, o, random.nextInt(4));
        }
        return this;
    }

    /** Igual que {@link #every} pero en ambos lados de la pista. */
    public TrackBuilder bothSides(int from, int to, int step, SpriteKind kind, double offset, double jitter) {
        every(from, to, step, kind, -Math.abs(offset), jitter);
        every(from + step / 2, to, step, kind, Math.abs(offset), jitter);
        return this;
    }

    // ---------- construcción ----------

    public Track build(TrackInfo info, TrackTheme theme) {
        if (Math.abs(lastY()) > 1) {
            road(SHORT, SHORT, SHORT, 0, -lastY() / TrackConstants.SEGMENT_LENGTH); // vuelve al nivel de largada
        }
        List<Segment> segments = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            double[] r = raw.get(i);
            segments.add(new Segment(i, r[0], r[1], r[2], r[3] > 0, r[4] > 0));
        }
        int n = segments.size();
        int[] checkpoints = new int[checkpointsPerLap];
        for (int i = 0; i < checkpointsPerLap; i++) {
            checkpoints[i] = (int) Math.round(n * (i + 1) / (double) (checkpointsPerLap + 1));
        }
        for (PendingSprite p : pendingSprites) {
            if (p.index >= 0 && p.index < n) {
                SpriteKind kind = mirror ? p.kind.mirrored() : p.kind;
                double offset = mirror ? -p.offset : p.offset;
                segments.get(p.index).addSprite(new TrackSprite(kind, offset, p.variant));
            }
        }
        segments.get(2).addSprite(new TrackSprite(SpriteKind.GANTRY, 0, 0));
        for (int cp : checkpoints) {
            segments.get(cp).addSprite(new TrackSprite(SpriteKind.CHECKPOINT, 0, 0));
        }
        if (autoChevrons) {
            placeChevrons(segments);
        }
        return new Track(info, segments, theme, checkpoints, mirror, computeMinimap(segments));
    }

    /** Carteles de flecha en el lado exterior antes de cada curva fuerte. */
    private static void placeChevrons(List<Segment> segments) {
        int n = segments.size();
        int cooldown = 0;
        for (int i = 0; i < n; i++) {
            if (cooldown > 0) {
                cooldown--;
                continue;
            }
            double c = segments.get((i + 20) % n).curve();
            if (Math.abs(c) >= MID - 0.5 && Math.abs(segments.get(i).curve()) < 1) {
                double side = c > 0 ? -1.35 : 1.35; // exterior de la curva
                SpriteKind kind = c > 0 ? SpriteKind.CHEVRON_RIGHT : SpriteKind.CHEVRON_LEFT;
                for (int k = 0; k < 4; k++) {
                    int idx = (i + k * 5) % n;
                    if (!segments.get(idx).walls()) {
                        segments.get(idx).addSprite(new TrackSprite(kind, side, 0));
                    }
                }
                cooldown = 80;
            }
        }
    }

    /** Integra la curvatura para dibujar el contorno, cerrando el error final. */
    private static float[][] computeMinimap(List<Segment> segments) {
        int n = segments.size();
        double totalCurve = 0;
        for (Segment s : segments) {
            totalCurve += s.curve();
        }
        double k = Math.abs(totalCurve) > 1 ? (2 * Math.PI) / totalCurve : 0.004;
        double[] xs = new double[n + 1];
        double[] ys = new double[n + 1];
        double heading = -Math.PI / 2;
        for (int i = 0; i < n; i++) {
            heading += segments.get(i).curve() * k;
            xs[i + 1] = xs[i] + Math.cos(heading);
            ys[i + 1] = ys[i] + Math.sin(heading);
        }
        double ex = xs[n] - xs[0];
        double ey = ys[n] - ys[0];
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            xs[i] -= ex * t;
            ys[i] -= ey * t;
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        double span = Math.max(maxX - minX, maxY - minY);
        double ox = (span - (maxX - minX)) / 2;
        double oy = (span - (maxY - minY)) / 2;
        float[][] pts = new float[n][2];
        for (int i = 0; i < n; i++) {
            pts[i][0] = (float) ((xs[i] - minX + ox) / span);
            pts[i][1] = (float) ((ys[i] - minY + oy) / span);
        }
        return pts;
    }

    private void add(double curve, double y) {
        double y1 = lastY();
        raw.add(new double[]{curve, y1, y, tunnel ? 1 : 0, walls ? 1 : 0});
    }

    private double lastY() {
        return raw.isEmpty() ? 0 : raw.get(raw.size() - 1)[2];
    }

    private static double easeIn(double a, double b, double p) {
        return a + (b - a) * p * p;
    }

    private static double easeInOut(double a, double b, double p) {
        return a + (b - a) * ((-Math.cos(p * Math.PI) / 2) + 0.5);
    }
}
