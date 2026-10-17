package com.daytona.domain.track.tracks;

import com.daytona.domain.track.Backdrop;
import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.TrackBuilder;
import com.daytona.domain.track.TrackDefinition;
import com.daytona.domain.track.TrackInfo;
import com.daytona.domain.track.TrackTheme;

/** Pista extra de montaña nevada con grandes desniveles. */
public final class AlpinePass implements TrackDefinition {

    @Override
    public TrackInfo info() {
        return new TrackInfo("alpine", "Alpine Pass", "MAESTRO",
                "Paso de montaña nevado: subidas enormes, túnel bajo la cumbre y bajadas a fondo.", 3);
    }

    @Override
    public TrackTheme theme() {
        return new TrackTheme(0x4F98DE, 0xDDEBF7, 0xE6EEF5,
                0xF2F6FA, 0xE3EBF2, 0xF5F5F5, 0x1F6FD1,
                0x58636E, 0x525C66, 0xFFFFFF,
                0xB8C4CF, 0x5A6470, 0x2C333A,
                0xFFFFF0, false, Backdrop.ALPS);
    }

    @Override
    public void layout(TrackBuilder b) {
        int a = b.size();
        b.straight(60);
        b.hill(100, 35);
        b.curve(120, -3);
        b.road(40, 80, 40, 2.5, 25);
        b.rollingHills(30, 20);
        forest(b, a, b.size());
        b.every(a + 5, a + 60, 25, SpriteKind.BILLBOARD, 1.5, 0);

        b.tunnel(true);
        b.road(30, 120, 30, -1.5, 0);
        b.tunnel(false);

        int c = b.size();
        b.road(40, 60, 40, 5, -30);
        b.sCurves(30, 3.5);
        b.road(50, 120, 50, -3.5, -20);
        b.hill(80, -10);
        b.road(30, 60, 30, -4, 0);
        b.straight(100);
        forest(b, c, b.size());
    }

    private static void forest(TrackBuilder b, int from, int to) {
        b.bothSides(from, to, 6, SpriteKind.SNOW_PINE, 1.3, 3.0);
        b.bothSides(from + 2, to, 17, SpriteKind.ROCK, 1.35, 1.0);
        b.bothSides(from + 4, to, 9, SpriteKind.PINE, 2.2, 3.0);
    }
}
