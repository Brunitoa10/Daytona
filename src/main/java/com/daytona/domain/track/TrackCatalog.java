package com.daytona.domain.track;

import com.daytona.domain.track.tracks.AlpinePass;
import com.daytona.domain.track.tracks.NeonHarbor;
import com.daytona.domain.track.tracks.RedRockCanyon;
import com.daytona.domain.track.tracks.SunriseSpeedway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Patrón Factory + caché: construye cada pista (normal o espejo) una sola vez. */
public final class TrackCatalog {

    private final List<TrackDefinition> definitions = java.util.Collections.unmodifiableList(java.util.Arrays.asList(
            new SunriseSpeedway(), new RedRockCanyon(), new NeonHarbor(), new AlpinePass()));
    private final Map<String, Track> cache = new HashMap<>();

    public List<TrackInfo> all() {
        return definitions.stream().map(TrackDefinition::info).collect(java.util.stream.Collectors.toList());
    }

    public int indexOf(String id) {
        for (int i = 0; i < definitions.size(); i++) {
            if (definitions.get(i).info().id().equals(id)) {
                return i;
            }
        }
        return 0;
    }

    public Track get(String id, boolean mirror) {
        return cache.computeIfAbsent(id + (mirror ? ":m" : ""), k -> build(definition(id), mirror));
    }

    public Track get(int index, boolean mirror) {
        return get(definitions.get(Math.floorMod(index, definitions.size())).info().id(), mirror);
    }

    public int size() {
        return definitions.size();
    }

    private TrackDefinition definition(String id) {
        return definitions.get(indexOf(id));
    }

    private static Track build(TrackDefinition def, boolean mirror) {
        TrackBuilder b = new TrackBuilder(mirror, def.info().id().hashCode());
        def.layout(b);
        return b.build(def.info(), def.theme());
    }
}
