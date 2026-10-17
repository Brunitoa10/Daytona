package com.daytona.render;

import com.daytona.domain.car.CarSpec;
import com.daytona.domain.track.Segment;
import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackSprite;
import com.daytona.render.art.CarArt;
import com.daytona.render.art.SceneryArt;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Caché de imágenes generadas (Flyweight). Convierte cada imagen al formato
 * compatible con la placa de video para que Java2D la dibuje acelerada.
 */
public final class SpriteAtlas {

    public static final int CAR_FRAMES = 7;

    private final Map<String, BufferedImage> scenery = new HashMap<>();
    private final Map<String, BufferedImage[]> cars = new HashMap<>();
    private final GraphicsConfiguration gc;

    public SpriteAtlas() {
        GraphicsConfiguration conf = null;
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                conf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            }
        } catch (RuntimeException ignored) {
            // sin pantalla: se usan imágenes comunes
        }
        this.gc = conf;
    }

    public BufferedImage scenery(SpriteKind kind, int variant, boolean night) {
        int v = Math.floorMod(variant, variants(kind));
        String key = kind.name() + ':' + v + ':' + night;
        return scenery.computeIfAbsent(key, k -> optimize(SceneryArt.render(kind, v, night)));
    }

    /**
     * @param yaw -1..1
     */
    public BufferedImage car(CarSpec spec, double yaw, boolean brake) {
        String key = spec.bodyRgb() + ":" + spec.stripeRgb() + ":" + spec.number();
        BufferedImage[] frames = cars.computeIfAbsent(key, k -> buildCar(spec));
        int idx = (int) Math.round((Math.max(-1, Math.min(1, yaw)) + 1) / 2 * (CAR_FRAMES - 1));
        return frames[idx * 2 + (brake ? 1 : 0)];
    }

    private BufferedImage[] buildCar(CarSpec spec) {
        BufferedImage[] frames = new BufferedImage[CAR_FRAMES * 2];
        for (int i = 0; i < CAR_FRAMES; i++) {
            double yaw = -1 + 2.0 * i / (CAR_FRAMES - 1);
            frames[i * 2] = optimize(CarArt.render(spec.bodyRgb(), spec.stripeRgb(), spec.number(), yaw, false));
            frames[i * 2 + 1] = optimize(CarArt.render(spec.bodyRgb(), spec.stripeRgb(), spec.number(), yaw, true));
        }
        return frames;
    }

    /** Genera de antemano todo lo que usa una pista para evitar tirones en carrera. */
    public void warmUp(Track track, Iterable<CarSpec> specs) {
        boolean night = track.theme().night();
        for (int i = 0; i < track.segmentCount(); i++) {
            Segment s = track.segment(i);
            for (TrackSprite sp : s.sprites()) {
                scenery(sp.kind(), sp.variant(), night);
            }
        }
        for (CarSpec spec : specs) {
            car(spec, 0, false);
        }
    }

    private static int variants(SpriteKind kind) {
        switch (kind) {
            case TREE:
            case ROCK:
            case CLIFF:
            case BUILDING:
            case TOWER:
            case BILLBOARD:
            case FLAG:
                return 4;
            case PALM:
            case CACTUS:
            case GRANDSTAND:
            case PINE:
            case SNOW_PINE:
                return 2;
            default:
                return 1;
        }
    }

    private BufferedImage optimize(BufferedImage src) {
        if (gc == null) {
            return src;
        }
        BufferedImage dst = gc.createCompatibleImage(src.getWidth(), src.getHeight(), Transparency.TRANSLUCENT);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }
}
