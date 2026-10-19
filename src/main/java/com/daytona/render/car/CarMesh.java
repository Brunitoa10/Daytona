package com.daytona.render.car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo 3D low-poly de un stock car (unidades de mundo, x = lateral, y = alto,
 * z = largo, frente hacia +z). Se construye una sola vez y lo comparten todos los
 * autos (Flyweight); la librea se aplica al dibujar.
 */
public final class CarMesh {

    /** Material de cada cara: define el color al dibujar. */
    public enum Material { BODY, STRIPE, GLASS, TIRE, RIM, TAIL, PLATE, DARK }

    /** Cara plana del modelo. */
    public static final class Face {
        private final double[] x;
        private final double[] y;
        private final double[] z;
        private final Material material;
        private final boolean decal;
        private final int layer;
        private final double nx;
        private final double ny;
        private final double nz;
        private final double cx;
        private final double cy;
        private final double cz;

        public double x(int k) { return x[k]; }
        public double y(int k) { return y[k]; }
        public double z(int k) { return z[k]; }
        public Material material() { return material; }
        public int layer() { return layer; }
        public double nx() { return nx; }
        public double ny() { return ny; }
        public double nz() { return nz; }
        public double cx() { return cx; }
        public double cy() { return cy; }
        public double cz() { return cz; }

        private Face(double[] pts, Material material, int layer, double centerY) {
            int n = pts.length / 3;
            x = new double[n];
            y = new double[n];
            z = new double[n];
            double sx = 0, sy = 0, sz = 0;
            for (int i = 0; i < n; i++) {
                x[i] = pts[i * 3];
                y[i] = pts[i * 3 + 1];
                z[i] = pts[i * 3 + 2];
                sx += x[i];
                sy += y[i];
                sz += z[i];
            }
            cx = sx / n;
            cy = sy / n;
            cz = sz / n;
            // normal por producto cruz, orientada hacia afuera del auto
            double ax = x[1] - x[0], ay = y[1] - y[0], az = z[1] - z[0];
            double bx = x[2] - x[0], by = y[2] - y[0], bz = z[2] - z[0];
            double mx = ay * bz - az * by;
            double my = az * bx - ax * bz;
            double mz = ax * by - ay * bx;
            double len = Math.sqrt(mx * mx + my * my + mz * mz);
            if (len < 1e-9) {
                len = 1;
            }
            mx /= len;
            my /= len;
            mz /= len;
            if (mx * cx + my * (cy - centerY) + mz * cz < 0) {
                mx = -mx;
                my = -my;
                mz = -mz;
            }
            nx = mx;
            ny = my;
            nz = mz;
            this.material = material;
            this.decal = layer > 0;
            this.layer = layer;
        }

        public int size() {
            return x.length;
        }
    }

    private static final double CENTER_Y = 200;
    private static final CarMesh STOCK_CAR = buildStockCar();

    private final List<Face> faces;
    private final int plateFace;
    private final int roofFace;

    private CarMesh(List<Face> faces, int plateFace, int roofFace) {
        this.faces = Collections.unmodifiableList(faces);
        this.plateFace = plateFace;
        this.roofFace = roofFace;
    }

    public static CarMesh stockCar() {
        return STOCK_CAR;
    }

    public List<Face> faces() {
        return faces;
    }

    /** Índice de la cara de la patente (para escribir el número). */
    public int plateFace() {
        return plateFace;
    }

    /** Índice del círculo del techo con el número de carrera. */
    public int roofFace() {
        return roofFace;
    }

    private static CarMesh buildStockCar() {
        Builder b = new Builder();
        // --- carrocería baja (cuña típica de stock car) ---
        b.face(Material.BODY, -320, 40, -625, 320, 40, -625, 320, 215, -625, -320, 215, -625);       // cola
        b.face(Material.BODY, -320, 40, -625, -300, 40, 625, -290, 160, 625, -312, 195, 250, -320, 215, -330, -320, 215, -625); // lateral izq
        b.face(Material.BODY, 320, 40, -625, 300, 40, 625, 290, 160, 625, 312, 195, 250, 320, 215, -330, 320, 215, -625);       // lateral der
        b.face(Material.BODY, -320, 215, -625, 320, 215, -625, 320, 215, -330, -320, 215, -330);     // baúl
        b.face(Material.BODY, -312, 195, 250, 312, 195, 250, 290, 160, 625, -290, 160, 625);         // capot
        b.face(Material.BODY, -300, 40, 625, 300, 40, 625, 290, 160, 625, -290, 160, 625);           // trompa
        b.face(Material.BODY, -320, 215, -330, -240, 215, -330, -240, 195, 250, -312, 195, 250);     // hombro izq
        b.face(Material.BODY, 320, 215, -330, 240, 215, -330, 240, 195, 250, 312, 195, 250);         // hombro der
        // --- habitáculo bajo ---
        b.face(Material.GLASS, -240, 215, -330, 240, 215, -330, 200, 345, -230, -200, 345, -230);    // luneta
        b.face(Material.BODY, -200, 345, -230, 200, 345, -230, 200, 345, 60, -200, 345, 60);         // techo
        b.face(Material.GLASS, -200, 345, 60, 200, 345, 60, 240, 195, 250, -240, 195, 250);          // parabrisas
        b.face(Material.GLASS, -240, 215, -330, -200, 345, -230, -200, 345, 60, -240, 195, 250);     // ventanas izq
        b.face(Material.GLASS, 240, 215, -330, 200, 345, -230, 200, 345, 60, 240, 195, 250);         // ventanas der
        // --- alerón ---
        b.face(Material.DARK, -315, 285, -615, 315, 285, -615, 315, 285, -525, -315, 285, -525);
        b.face(Material.DARK, -315, 255, -616, 315, 255, -616, 315, 285, -616, -315, 285, -616);
        b.face(Material.DARK, -280, 215, -598, -250, 215, -598, -250, 270, -598, -280, 270, -598);
        b.face(Material.DARK, 250, 215, -598, 280, 215, -598, 280, 270, -598, 250, 270, -598);
        // --- ruedas ---
        wheel(b, -1, -400);
        wheel(b, 1, -400);
        wheel(b, -1, 400);
        wheel(b, 1, 400);
        // --- franjas de carrera ---
        b.decal(Material.STRIPE, -75, 216, -625, -25, 216, -625, -25, 216, -330, -75, 216, -330);
        b.decal(Material.STRIPE, 25, 216, -625, 75, 216, -625, 75, 216, -330, 25, 216, -330);
        b.decal(Material.STRIPE, -75, 346, -230, -25, 346, -230, -25, 346, 60, -75, 346, 60);
        b.decal(Material.STRIPE, 25, 346, -230, 75, 346, -230, 75, 346, 60, 25, 346, 60);
        b.decal(Material.STRIPE, -75, 196, 250, -25, 196, 250, -22, 161, 625, -70, 161, 625);
        b.decal(Material.STRIPE, 25, 196, 250, 75, 196, 250, 70, 161, 625, 22, 161, 625);
        b.decal(Material.STRIPE, -321, 70, -560, -321, 70, 560, -321, 115, 560, -321, 115, -560);
        b.decal(Material.STRIPE, 321, 70, -560, 321, 70, 560, 321, 115, 560, 321, 115, -560);
        b.decal(Material.STRIPE, -75, 40, -626, -25, 40, -626, -25, 215, -626, -75, 215, -626);
        b.decal(Material.STRIPE, 25, 40, -626, 75, 40, -626, 75, 215, -626, 25, 215, -626);
        // --- cola: panel negro, ópticas, paragolpes y patente ---
        b.decal(Material.DARK, -318, 125, -627, 318, 125, -627, 318, 205, -627, -318, 205, -627);
        b.decal(Material.DARK, -320, 40, -627, 320, 40, -627, 320, 78, -627, -320, 78, -627);
        b.decal2(Material.TAIL, -305, 135, -628, -130, 135, -628, -130, 195, -628, -305, 195, -628);
        b.decal2(Material.TAIL, 130, 135, -628, 305, 135, -628, 305, 195, -628, 130, 195, -628);
        int plate = b.decal2(Material.PLATE, -80, 82, -628, 80, 82, -628, 80, 122, -628, -80, 122, -628);
        // --- círculo blanco con el número en el techo ---
        double[] disc = new double[8 * 3];
        for (int k = 0; k < 8; k++) {
            double a = Math.PI * 2 * k / 8 + Math.PI / 8;
            disc[k * 3] = Math.cos(a) * 115;
            disc[k * 3 + 1] = 347;
            disc[k * 3 + 2] = -85 + Math.sin(a) * 120;
        }
        int roof = b.decal2(Material.PLATE, disc);
        return new CarMesh(b.faces, plate, roof);
    }

    private static void wheel(Builder b, int side, double zc) {
        double xo = side * 340;
        double xi = side * 300;
        double r = 115;
        b.face(Material.TIRE, xo, 0, zc - r, xo, 0, zc + r, xo, 2 * r, zc + r, xo, 2 * r, zc - r);   // costado
        b.face(Material.TIRE, xi, 0, zc - r, xo, 0, zc - r, xo, 2 * r, zc - r, xi, 2 * r, zc - r);   // atrás
        b.face(Material.TIRE, xi, 2 * r, zc - r, xo, 2 * r, zc - r, xo, 2 * r, zc + r, xi, 2 * r, zc + r); // arriba
        b.decal(Material.RIM, xo + side, 50, zc - 60, xo + side, 50, zc + 60, xo + side, 180, zc + 60, xo + side, 180, zc - 60);
    }

    private static final class Builder {
        private final List<Face> faces = new ArrayList<>();

        private int face(Material m, double... pts) {
            faces.add(new Face(pts, m, 0, CENTER_Y));
            return faces.size() - 1;
        }

        private int decal(Material m, double... pts) {
            faces.add(new Face(pts, m, 1, CENTER_Y));
            return faces.size() - 1;
        }

        /** Calco que va encima de otro calco. */
        private int decal2(Material m, double... pts) {
            faces.add(new Face(pts, m, 2, CENTER_Y));
            return faces.size() - 1;
        }
    }
}
