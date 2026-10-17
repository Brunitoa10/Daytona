package com.daytona.domain.track;

/** Datos descriptivos de una pista. */
public final class TrackInfo {
    private final String id;
    private final String name;
    private final String level;
    private final String description;
    private final int defaultLaps;

    public TrackInfo(String id, String name, String level, String description, int defaultLaps) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.description = description;
        this.defaultLaps = defaultLaps;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String level() {
        return level;
    }

    public String description() {
        return description;
    }

    public int defaultLaps() {
        return defaultLaps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrackInfo)) {
            return false;
        }
        TrackInfo other = (TrackInfo) o;
        return java.util.Objects.equals(id, other.id) && java.util.Objects.equals(name, other.name) && java.util.Objects.equals(level, other.level) && java.util.Objects.equals(description, other.description) && java.util.Objects.equals(defaultLaps, other.defaultLaps);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, level, description, defaultLaps);
    }
}
