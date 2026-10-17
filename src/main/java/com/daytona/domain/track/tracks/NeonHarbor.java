package com.daytona.domain.track.tracks;

import com.daytona.domain.track.Backdrop;
import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.TrackBuilder;
import com.daytona.domain.track.TrackDefinition;
import com.daytona.domain.track.TrackInfo;
import com.daytona.domain.track.TrackTheme;

/** Circuito urbano nocturno: curvas cerradas entre edificios y un paso bajo nivel. */
public final class NeonHarbor implements TrackDefinition {

    @Override
    public TrackInfo info() {
        return new TrackInfo("harbor", "Neon Harbor", "EXPERTO",
                "Calles del puerto de noche: curvas de 90°, puente, túnel y horquilla final.", 3);
    }

    @Override
    public TrackTheme theme() {
        return new TrackTheme(0x05051A, 0x2A1650, 0x1A1238,
                0x2C2C38, 0x282832, 0xF2F2F2, 0xFF2D7A,
                0x3C3C46, 0x383842, 0xFFD84A,
                0x50506A, 0x2A2A3A, 0x15151F,
                0xE8F0FF, true, Backdrop.CITY_SKYLINE);
    }

    @Override
    public void layout(TrackBuilder b) {
        b.walls(true);
        int a = b.size();
        b.straight(80);
        b.road(20, 30, 20, TrackBuilder.HARD, 0);
        b.straight(60);
        b.road(20, 30, 20, -TrackBuilder.HARD, 0);
        b.straight(100);
        b.road(30, 60, 30, -3.5, 0);
        city(b, a, b.size());

        int bridge = b.size();
        b.hill(60, 15);
        b.hill(60, -15);
        b.every(bridge, b.size(), 5, SpriteKind.LAMP, 1.3, 0);
        b.every(bridge + 2, b.size(), 5, SpriteKind.LAMP, -1.3, 0);

        b.tunnel(true);
        b.road(25, 80, 25, 2.5, -10);
        b.tunnel(false);

        int c = b.size();
        b.sCurves(20, 4.5);
        b.road(25, 50, 25, -5.5, 10);
        b.straight(120);
        b.road(30, 60, 30, -3, 0);
        b.road(20, 20, 20, 4, 0);
        b.straight(80);
        city(b, c, b.size());
    }

    private static void city(TrackBuilder b, int from, int to) {
        b.bothSides(from, to, 14, SpriteKind.BUILDING, 1.45, 0.6);
        b.every(from + 3, to, 7, SpriteKind.LAMP, 1.3, 0);
        b.every(from + 6, to, 7, SpriteKind.LAMP, -1.3, 0);
        b.every(from + 20, to, 45, SpriteKind.BILLBOARD, -1.4, 0);
        b.every(from + 40, to, 60, SpriteKind.TOWER, 3.2, 1.5);
    }
}
