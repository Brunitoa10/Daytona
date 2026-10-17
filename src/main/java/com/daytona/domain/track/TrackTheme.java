package com.daytona.domain.track;

/** Paleta de colores de una pista (RGB enteros para no depender de AWT en el dominio). */
public final class TrackTheme {
    private final int skyTop;
    private final int skyBottom;
    private final int fog;
    private final int grassLight;
    private final int grassDark;
    private final int rumbleLight;
    private final int rumbleDark;
    private final int roadLight;
    private final int roadDark;
    private final int lane;
    private final int wall;
    private final int tunnelWall;
    private final int tunnelCeiling;
    private final int sun;
    private final boolean night;
    private final Backdrop backdrop;

    public TrackTheme(int skyTop, int skyBottom, int fog, int grassLight, int grassDark, int rumbleLight, int rumbleDark, int roadLight, int roadDark, int lane, int wall, int tunnelWall, int tunnelCeiling, int sun, boolean night, Backdrop backdrop) {
        this.skyTop = skyTop;
        this.skyBottom = skyBottom;
        this.fog = fog;
        this.grassLight = grassLight;
        this.grassDark = grassDark;
        this.rumbleLight = rumbleLight;
        this.rumbleDark = rumbleDark;
        this.roadLight = roadLight;
        this.roadDark = roadDark;
        this.lane = lane;
        this.wall = wall;
        this.tunnelWall = tunnelWall;
        this.tunnelCeiling = tunnelCeiling;
        this.sun = sun;
        this.night = night;
        this.backdrop = backdrop;
    }

    public int skyTop() {
        return skyTop;
    }

    public int skyBottom() {
        return skyBottom;
    }

    public int fog() {
        return fog;
    }

    public int grassLight() {
        return grassLight;
    }

    public int grassDark() {
        return grassDark;
    }

    public int rumbleLight() {
        return rumbleLight;
    }

    public int rumbleDark() {
        return rumbleDark;
    }

    public int roadLight() {
        return roadLight;
    }

    public int roadDark() {
        return roadDark;
    }

    public int lane() {
        return lane;
    }

    public int wall() {
        return wall;
    }

    public int tunnelWall() {
        return tunnelWall;
    }

    public int tunnelCeiling() {
        return tunnelCeiling;
    }

    public int sun() {
        return sun;
    }

    public boolean night() {
        return night;
    }

    public Backdrop backdrop() {
        return backdrop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrackTheme)) {
            return false;
        }
        TrackTheme other = (TrackTheme) o;
        return java.util.Objects.equals(skyTop, other.skyTop) && java.util.Objects.equals(skyBottom, other.skyBottom) && java.util.Objects.equals(fog, other.fog) && java.util.Objects.equals(grassLight, other.grassLight) && java.util.Objects.equals(grassDark, other.grassDark) && java.util.Objects.equals(rumbleLight, other.rumbleLight) && java.util.Objects.equals(rumbleDark, other.rumbleDark) && java.util.Objects.equals(roadLight, other.roadLight) && java.util.Objects.equals(roadDark, other.roadDark) && java.util.Objects.equals(lane, other.lane) && java.util.Objects.equals(wall, other.wall) && java.util.Objects.equals(tunnelWall, other.tunnelWall) && java.util.Objects.equals(tunnelCeiling, other.tunnelCeiling) && java.util.Objects.equals(sun, other.sun) && java.util.Objects.equals(night, other.night) && java.util.Objects.equals(backdrop, other.backdrop);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(skyTop, skyBottom, fog, grassLight, grassDark, rumbleLight, rumbleDark, roadLight, roadDark, lane, wall, tunnelWall, tunnelCeiling, sun, night, backdrop);
    }
}
