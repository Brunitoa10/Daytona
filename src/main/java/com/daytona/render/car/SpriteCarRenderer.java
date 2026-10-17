package com.daytona.render.car;

import com.daytona.render.CarSprite;
import com.daytona.render.SpriteAtlas;
import com.daytona.render.road.RoadFrame;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Autos como imágenes 2D pre-dibujadas (estilo "super scaler" de fines de los 80). */
public final class SpriteCarRenderer implements CarRenderer {

    public static final double CAR_WORLD_WIDTH = 760;

    private final SpriteAtlas atlas;

    public SpriteCarRenderer(SpriteAtlas atlas) {
        this.atlas = atlas;
    }

    @Override
    public boolean draw(Graphics2D g, RoadFrame f, int i, double pct, CarSprite c, Rectangle bounds) {
        double halfW = f.width() / 2.0;
        double scale = f.scale1(i) + (f.scale2(i) - f.scale1(i)) * pct;
        double cx = f.x1(i) + (f.x2(i) - f.x1(i)) * pct + scale * c.x() * RoadFrame.ROAD * halfW;
        double cy = f.y1(i) + (f.y2(i) - f.y1(i)) * pct;
        BufferedImage img = atlas.car(c.spec(), c.yaw(), c.braking());
        double destW = CAR_WORLD_WIDTH * scale * halfW;
        double destH = destW * img.getHeight() / img.getWidth();
        if (destW < 2) {
            return false;
        }
        int left = (int) (cx - destW / 2);
        int top = (int) (cy - destH);
        Composite base = g.getComposite();
        float alpha = c.alpha();
        if (f.fog(i) > 0 || alpha < 1f) {
            float fa = f.fogAlpha(i).getAlpha() * alpha;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.05f, fa)));
        }
        g.drawImage(img, left, top, (int) Math.ceil(destW), (int) Math.ceil(destH), null);
        g.setComposite(base);
        bounds.setBounds(left, top, (int) destW, (int) destH);
        return true;
    }
}
