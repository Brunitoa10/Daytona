package com.daytona.domain.track.tracks;

import com.daytona.domain.track.Backdrop;
import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.TrackBuilder;
import com.daytona.domain.track.TrackDefinition;
import com.daytona.domain.track.TrackInfo;
import com.daytona.domain.track.TrackTheme;

/** Cañón al atardecer: subidas, eses, túnel y una horquilla. */
public final class RedRockCanyon implements TrackDefinition {

    @Override
    public TrackInfo info() {
        return new TrackInfo("canyon", "Red Rock Canyon", "AVANZADO",
                "Cañón rojo con colinas, eses rápidas, un túnel y una horquilla traicionera.", 4);
    }

    @Override
    public TrackTheme theme() {
        return new TrackTheme(0x3B2A6B, 0xFF8A3D, 0xDE9460,
                0xC9844A, 0xBD7A43, 0xF5F5F5, 0xD43A2F,
                0x605B57, 0x5A5551, 0xF0E6C8,
                0x8A4B2A, 0x5A3420, 0x2B1A10,
                0xFFD27A, false, Backdrop.CANYON);
    }

    @Override
    public void layout(TrackBuilder b) {
        int start = b.size();
        b.straight(60);
        b.hill(75, 20);
        b.curve(90, 3.5);
        b.every(start + 6, b.size(), 28, SpriteKind.BILLBOARD, -1.5, 0);
        b.bothSides(start, b.size(), 9, SpriteKind.CACTUS, 1.35, 2.5);

        int s = b.size();
        b.sCurves(25, 3.0);
        b.rollingHills(25, 18);
        b.road(30, 60, 30, -4.5, -10);
        b.bothSides(s, b.size(), 10, SpriteKind.CLIFF, 1.7, 1.2);
        b.bothSides(s + 4, b.size(), 13, SpriteKind.ROCK, 1.3, 0.8);

        b.tunnel(true);
        b.road(30, 90, 30, 1.5, 0);
        b.tunnel(false);

        int h = b.size();
        b.road(30, 60, 30, -5.5, 0);
        b.hill(90, -25);
        b.road(40, 100, 40, 2.5, 10);
        b.bothSides(h, b.size(), 8, SpriteKind.CACTUS, 1.35, 3);
        b.bothSides(h + 3, b.size(), 15, SpriteKind.CLIFF, 2.0, 1.5);

        int f = b.size();
        b.straight(90);
        b.road(30, 60, 30, -3.5, 0);
        b.straight(60);
        b.bothSides(f, b.size(), 12, SpriteKind.ROCK, 1.4, 2);
        b.every(f + 10, b.size(), 30, SpriteKind.BILLBOARD, 1.5, 0);
    }
}
