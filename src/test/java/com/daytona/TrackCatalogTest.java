package com.daytona;

import com.daytona.domain.track.Track;
import com.daytona.domain.track.TrackCatalog;
import com.daytona.domain.track.TrackInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrackCatalogTest {

    private final TrackCatalog catalog = new TrackCatalog();

    @Test
    public void allTracksBuildWithValidData() {
        assertEquals(4, catalog.size());
        for (TrackInfo info : catalog.all()) {
            Track t = catalog.get(info.id(), false);
            assertTrue(t.segmentCount() > 500, info.id() + " muy corta");
            for (int cp : t.checkpoints()) {
                assertTrue(cp > 0 && cp < t.segmentCount());
            }
            for (float[] p : t.minimap()) {
                assertTrue(p[0] >= -0.01 && p[0] <= 1.01 && p[1] >= -0.01 && p[1] <= 1.01);
            }
            assertEquals(0, t.segment(t.segmentCount() - 1).y2(), 1.0, "la pista debe cerrar en altura");
        }
    }

    @Test
    public void mirrorFlipsCurves() {
        Track normal = catalog.get("canyon", false);
        Track mirror = catalog.get("canyon", true);
        for (int i = 0; i < normal.segmentCount(); i++) {
            assertEquals(normal.segment(i).curve(), -mirror.segment(i).curve(), 1e-9);
        }
    }
}
