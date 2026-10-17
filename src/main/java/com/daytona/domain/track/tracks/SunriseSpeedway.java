package com.daytona.domain.track.tracks;

import com.daytona.domain.track.Backdrop;
import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.TrackBuilder;
import com.daytona.domain.track.TrackDefinition;
import com.daytona.domain.track.TrackInfo;
import com.daytona.domain.track.TrackTheme;

/** Óvalo de principiantes: rectas largas, curvas peraltadas y muros. */
public final class SunriseSpeedway implements TrackDefinition {

    @Override
    public TrackInfo info() {
        return new TrackInfo("sunrise", "Sunrise Speedway", "PRINCIPIANTE",
                "Óvalo de alta velocidad junto al mar. Ideal para aprender rebufo y trazadas.", 6);
    }

    @Override
    public TrackTheme theme() {
        return new TrackTheme(0x1450C8, 0x9FD0F5, 0xC4E0F5,
                0x41A03B, 0x398E32, 0xF2F2F2, 0xC8102E,
                0x6E6E6E, 0x686868, 0xEEEEEE,
                0xDCDCDC, 0x666666, 0x333333,
                0xFFE38A, false, Backdrop.COAST_HILLS);
    }

    @Override
    public void layout(TrackBuilder b) {
        b.walls(true).checkpointsPerLap(1);

        int front = b.size();
        b.straight(120);
        b.every(front + 8, front + 120, 11, SpriteKind.GRANDSTAND, 1.5, 0);
        b.every(front + 4, front + 120, 24, SpriteKind.BILLBOARD, -1.45, 0);

        int t12 = b.size();
        b.road(50, 160, 50, -3.0, 0);
        b.every(t12, t12 + 260, 9, SpriteKind.LAMP, 1.35, 0);
        b.every(t12 + 5, t12 + 260, 26, SpriteKind.FLAG, -1.4, 0);

        int back = b.size();
        b.straight(260);
        b.every(back, back + 260, 30, SpriteKind.BILLBOARD, 1.45, 0);
        b.every(back + 6, back + 260, 18, SpriteKind.PALM, 2.4, 1.5);
        b.every(back + 12, back + 260, 22, SpriteKind.TIRES, -1.4, 0);

        int t34 = b.size();
        b.road(50, 160, 50, -3.0, 0);
        b.every(t34, t34 + 260, 9, SpriteKind.LAMP, 1.35, 0);

        int dog = b.size();
        b.road(20, 40, 20, -1.2, 0);
        b.every(dog, dog + 80, 14, SpriteKind.PALM, 2.0, 1.0);
        int last = b.size();
        b.straight(100);
        b.every(last + 4, last + 100, 11, SpriteKind.GRANDSTAND, 1.5, 0);
    }
}
