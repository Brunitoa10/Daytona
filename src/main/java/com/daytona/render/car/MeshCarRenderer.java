package com.daytona.render.car;

import com.daytona.render.CarSprite;
import com.daytona.render.FogPalette;
import com.daytona.render.road.RoadFrame;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Autos en polígonos 3D con sombreado plano, como las placas arcade de 1994.
 * Cada vértice se proyecta con el mismo motor de la pista, así el auto sigue
 * curvas y lomas; las caras se ordenan de atrás hacia adelante (pintor) y se
 * descartan las que miran hacia el otro lado (back-face culling).
 */
public final class MeshCarRenderer implements CarRenderer {

    private static final double LX = 0.30, LY = 0.82, LZ = -0.48; // luz (ya casi normalizada)
    private static final double MAX_YAW = 0.38;                   // radianes
    private static final int SHADES = 16;
    /**
     * En el mundo pseudo-3D las distancias hacia adelante están "comprimidas" respecto
     * de las laterales; el modelo se achata en z para que se vea con proporciones reales.
     */
    private static final double Z_SCALE = 0.2;
    /** Exagera un poco la altura: la cámara arcade va alta y así la cola se lee mejor. */
    private static final double Y_SCALE = 1.45;

    private final CarMesh mesh = CarMesh.stockCar();
    private final boolean smooth;
    private final int maxPts = 10;
    private final double[] tmp = new double[3];
    private final int[] px = new int[maxPts];
    private final int[] py = new int[maxPts];
    private final int faceCount = mesh.faces().size();
    private final int[][] sx = new int[faceCount][maxPts];
    private final int[][] sy = new int[faceCount][maxPts];
    private final double[] depth = new double[faceCount];
    private final double[] light = new double[faceCount];
    private final int[] order = new int[faceCount];
    private final Font plateFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    /** @param smooth true = bordes suavizados (estilo HD); false = bordes duros (1994) */
    public MeshCarRenderer(boolean smooth) {
        this.smooth = smooth;
    }

    @Override
    public boolean draw(Graphics2D g, RoadFrame f, int seg, double pct, CarSprite car, Rectangle bounds) {
        double relZ = f.relativeZ(car.z());
        double lateral = car.x() * RoadFrame.ROAD;
        double yaw = Math.max(-1, Math.min(1, car.yaw())) * MAX_YAW;
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        List<CarMesh.Face> faces = mesh.faces();
        int visible = 0;
        for (int fi = 0; fi < faceCount; fi++) {
            CarMesh.Face face = faces.get(fi);
            double nx = face.nx * cos + face.nz * sin;
            double nz = (-face.nx * sin + face.nz * cos) / Z_SCALE;
            double ny = face.ny / Y_SCALE;
            double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= nl;
            ny /= nl;
            nz /= nl;
            double fcz = (-face.cx * sin + face.cz * cos) * Z_SCALE;
            double fcx = face.cx * cos + face.cz * sin;
            if (!f.projectPoint(relZ + fcz, lateral + fcx, face.cy * Y_SCALE, tmp)) {
                return false; // demasiado cerca o fuera del rango: no se dibuja este frame
            }
            // vector cámara -> cara reconstruido desde la proyección
            double scale = tmp[2];
            double vx = (tmp[0] - f.width() / 2.0) / (scale * f.width() / 2.0);
            double vy = (f.height() / 2.0 - tmp[1]) / (scale * f.height() / 2.0);
            double vz = relZ + fcz;
            if (nx * vx + ny * vy + nz * vz >= 0) {
                continue; // mira para el otro lado
            }
            int n = face.size();
            for (int k = 0; k < n; k++) {
                double rx = face.x[k] * cos + face.z[k] * sin;
                double rz = (-face.x[k] * sin + face.z[k] * cos) * Z_SCALE;
                if (!f.projectPoint(relZ + rz, lateral + rx, face.y[k] * Y_SCALE, tmp)) {
                    return false;
                }
                int ix = (int) Math.round(tmp[0]);
                int iy = (int) Math.round(tmp[1]);
                sx[fi][k] = ix;
                sy[fi][k] = iy;
                minX = Math.min(minX, ix);
                maxX = Math.max(maxX, ix);
                minY = Math.min(minY, iy);
                maxY = Math.max(maxY, iy);
            }
            depth[fi] = vz - face.layer * 4;
            light[fi] = 0.42 + 0.58 * Math.max(0, nx * LX + ny * LY + nz * LZ);
            order[visible++] = fi;
        }
        if (visible == 0 || maxX < 0 || minX > f.width() || maxY < 0 || minY > f.height()) {
            return false;
        }
        sortFarToNear(visible);

        Composite base = g.getComposite();
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                smooth ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        float alpha = car.alpha();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        drawShadow(g, f, relZ, lateral, cos, sin);
        int level = f.fog(seg);
        FogPalette pal = f.palette();
        for (int k = 0; k < visible; k++) {
            int fi = order[k];
            CarMesh.Face face = faces.get(fi);
            g.setColor(pal.get(shade(face.material, car, light[fi]), level));
            g.fillPolygon(sx[fi], sy[fi], face.size());
            if (fi == mesh.plateFace() || fi == mesh.roofFace()) {
                drawNumber(g, fi, car, face.size());
            }
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        g.setComposite(base);
        bounds.setBounds(minX, minY, maxX - minX, maxY - minY);
        return true;
    }

    private void drawShadow(Graphics2D g, RoadFrame f, double relZ, double lateral, double cos, double sin) {
        double[][] corners = {{-370, -690}, {370, -690}, {370, 690}, {-370, 690}};
        for (int k = 0; k < 4; k++) {
            double rx = corners[k][0] * cos + corners[k][1] * sin;
            double rz = (-corners[k][0] * sin + corners[k][1] * cos) * Z_SCALE;
            if (!f.projectPoint(relZ + rz, lateral + rx, 2, tmp)) {
                return;
            }
            px[k] = (int) Math.round(tmp[0]);
            py[k] = (int) Math.round(tmp[1]);
        }
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(Color.BLACK);
        g.fillPolygon(px, py, 4);
        g.setComposite(old);
    }

    private void drawNumber(Graphics2D g, int fi, CarSprite car, int n) {
        int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
        for (int k = 0; k < n; k++) {
            x0 = Math.min(x0, sx[fi][k]);
            x1 = Math.max(x1, sx[fi][k]);
            y0 = Math.min(y0, sy[fi][k]);
            y1 = Math.max(y1, sy[fi][k]);
        }
        int h = y1 - y0;
        if (h < 8) {
            return;
        }
        g.setFont(plateFont.deriveFont(Font.BOLD, h * 0.8f));
        FontMetrics fm = g.getFontMetrics();
        String num = Integer.toString(car.spec().number());
        if (fm.stringWidth(num) > (x1 - x0) * 0.95) {
            g.setFont(plateFont.deriveFont(Font.BOLD, h * 0.8f * (x1 - x0) * 0.9f / fm.stringWidth(num)));
            fm = g.getFontMetrics();
        }
        g.setColor(new Color(0x111111));
        g.drawString(num, (x0 + x1) / 2 - fm.stringWidth(num) / 2, (y0 + y1) / 2 + fm.getAscent() / 2 - fm.getDescent() / 2);
    }

    /** Ordena por profundidad (inserción: pocas caras, sin crear objetos). */
    private void sortFarToNear(int n) {
        for (int i = 1; i < n; i++) {
            int v = order[i];
            int j = i - 1;
            while (j >= 0 && depth[order[j]] < depth[v]) {
                order[j + 1] = order[j];
                j--;
            }
            order[j + 1] = v;
        }
    }

    private static int shade(CarMesh.Material m, CarSprite car, double light) {
        int rgb;
        boolean emissive = false;
        switch (m) {
            case BODY:
                rgb = car.spec().bodyRgb();
                break;
            case STRIPE:
                rgb = car.spec().stripeRgb();
                break;
            case GLASS:
                rgb = 0x2A3A52;
                break;
            case TIRE:
                rgb = 0x161616;
                break;
            case RIM:
                rgb = 0xA8AEB5;
                break;
            case TAIL:
                rgb = car.braking() ? 0xFF4030 : 0x8E1010;
                emissive = car.braking();
                break;
            case PLATE:
                rgb = 0xF0F0F0;
                emissive = true;
                break;
            default:
                rgb = 0x262626;
                break;
        }
        if (emissive) {
            return rgb;
        }
        double q = Math.round(light * SHADES) / (double) SHADES;
        int r = (int) Math.min(255, ((rgb >> 16) & 0xFF) * q);
        int gg = (int) Math.min(255, ((rgb >> 8) & 0xFF) * q);
        int b = (int) Math.min(255, (rgb & 0xFF) * q);
        return (r << 16) | (gg << 8) | b;
    }
}
