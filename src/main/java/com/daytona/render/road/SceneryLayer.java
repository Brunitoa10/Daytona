package com.daytona.render.road;

import com.daytona.domain.track.SpriteKind;
import com.daytona.domain.track.TrackSprite;
import com.daytona.render.SpriteAtlas;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Objetos de escenario como carteles 2D (árboles, edificios, tribunas, pórticos). */
public final class SceneryLayer implements SegmentLayer {

    private final SpriteAtlas atlas;

    public SceneryLayer(SpriteAtlas atlas) {
        this.atlas = atlas;
    }

    @Override
    public void paint(Graphics2D g, RoadFrame f, int i) {
        List<TrackSprite> list = f.segment(i).sprites();
        if (list.isEmpty()) {
            return;
        }
        int w = f.width();
        double scale = f.scale1(i);
        double halfW = w / 2.0;
        boolean night = f.theme().night();
        Composite base = g.getComposite();
        for (TrackSprite sp : list) {
            BufferedImage img = atlas.scenery(sp.kind(), sp.variant(), night);
            double destW = sp.kind().worldWidth() * scale * halfW;
            double destH = destW * img.getHeight() / img.getWidth();
            if (destH < 1) {
                continue;
            }
            double sx = f.x1(i) + scale * sp.offset() * RoadFrame.ROAD * halfW;
            double left = sp.offset() < 0 ? sx - destW : sp.offset() > 0 ? sx : sx - destW / 2;
            if (sp.kind() == SpriteKind.GANTRY || sp.kind() == SpriteKind.CHECKPOINT) {
                left = f.x1(i) - destW / 2;
            }
            if (left > w || left + destW < 0 || f.y1(i) - destH > f.height()) {
                continue;
            }
            if (f.fog(i) > 0) {
                g.setComposite(f.fogAlpha(i));
            }
            g.drawImage(img, (int) left, (int) (f.y1(i) - destH), (int) Math.ceil(destW), (int) Math.ceil(destH), null);
            if (f.fog(i) > 0) {
                g.setComposite(base);
            }
        }
    }
}
