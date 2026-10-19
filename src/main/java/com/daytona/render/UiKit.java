package com.daytona.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Tipografías, colores y primitivas de interfaz compartidas por todas las pantallas. */
public final class UiKit {

    public enum Align { LEFT, CENTER, RIGHT }

    public static final Color ACCENT = new Color(0xFFC21A);
    public static final Color ACCENT2 = new Color(0xFF4D2E);
    public static final Color TEXT = new Color(0xF4F4F4);
    public static final Color MUTED = new Color(0xA9B4C2);
    public static final Color PANEL = new Color(10, 14, 28, 190);
    public static final Color GOOD = new Color(0x4CD964);
    public static final Color BAD = new Color(0xFF3B30);

    private static final String DISPLAY_FAMILY;
    private static final String BODY_FAMILY;
    private static final Map<String, Font> FONTS = new HashMap<>();

    static {
        Set<String> available = new HashSet<>();
        try {
            available.addAll(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        } catch (RuntimeException ignored) {
            // sin fuentes del sistema
        }
        DISPLAY_FAMILY = first(available, "Impact", "Arial Black", "DejaVu Sans", Font.SANS_SERIF);
        BODY_FAMILY = first(available, "Segoe UI", "Arial", "DejaVu Sans", Font.SANS_SERIF);
    }

    private UiKit() {
    }

    private static String first(Set<String> available, String... names) {
        for (String n : names) {
            if (available.contains(n)) {
                return n;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Escala de interfaz relativa a 540 px de alto. */
    public static double scale(int h) {
        return h / 540.0;
    }

    public static Font display(float px) {
        return font(DISPLAY_FAMILY, DISPLAY_FAMILY.equals("Impact") ? Font.PLAIN : Font.BOLD | Font.ITALIC, px);
    }

    public static Font body(float px) {
        return font(BODY_FAMILY, Font.PLAIN, px);
    }

    public static Font bodyBold(float px) {
        return font(BODY_FAMILY, Font.BOLD, px);
    }

    private static Font font(String family, int style, float px) {
        int size = Math.max(6, Math.round(px));
        return FONTS.computeIfAbsent(family + style + ":" + size, k -> new Font(family, style, size));
    }

    public static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static int text(Graphics2D g, String s, double x, double y, Font f, Color c, Align align) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s);
        int dx;
        switch (align) {
            case LEFT:
                dx = 0;
                break;
            case CENTER:
                dx = -w / 2;
                break;
            case RIGHT:
                dx = -w;
                break;
            default:
                throw new IllegalStateException(String.valueOf(align));
        }
        g.setColor(c);
        g.drawString(s, (int) x + dx, (int) y);
        return w;
    }

    /** Texto con sombra marcada estilo arcade. */
    public static int shadowText(Graphics2D g, String s, double x, double y, Font f, Color c, Align align) {
        int off = Math.max(1, f.getSize() / 14);
        text(g, s, x + off, y + off, f, new Color(0, 0, 0, 170), align);
        return text(g, s, x, y, f, c, align);
    }

    private static final class Glyphs {
        private final java.awt.Shape shape;
        private final java.awt.Shape outline;
        private final int width;

        private Glyphs(java.awt.Shape shape, java.awt.Shape outline, int width) {
            this.shape = shape;
            this.outline = outline;
            this.width = width;
        }

        public java.awt.Shape shape() {
            return shape;
        }

        public java.awt.Shape outline() {
            return outline;
        }

        public int width() {
            return width;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Glyphs)) {
                return false;
            }
            Glyphs other = (Glyphs) o;
            return java.util.Objects.equals(shape, other.shape) && java.util.Objects.equals(outline, other.outline) && java.util.Objects.equals(width, other.width);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(shape, outline, width);
        }
    }

    private static final int GLYPH_CACHE = 600;
    private static final Map<String, Glyphs> GLYPHS = new java.util.LinkedHashMap<String, Glyphs>(256, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Glyphs> e) {
            return size() > GLYPH_CACHE;
        }
    };
    private static final Map<Integer, BasicStroke> OUTLINES = new HashMap<>();
    private static final Color OUTLINE_COLOR = new Color(0, 0, 0, 200);

    /** Texto con contorno (para números grandes sobre la pista). Las formas se cachean. */
    public static void outlinedText(Graphics2D g, String s, double x, double y, Font f, Color fill, Align align) {
        Glyphs gl = GLYPHS.computeIfAbsent(f.getFontName() + f.getStyle() + f.getSize() + "|" + s, k -> {
            java.awt.font.GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), s);
            java.awt.Shape shape = gv.getOutline();
            BasicStroke stroke = OUTLINES.computeIfAbsent(f.getSize(),
                    size -> new BasicStroke(Math.max(2f, size / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            return new Glyphs(shape, stroke.createStrokedShape(shape), (int) Math.ceil(gv.getLogicalBounds().getWidth()));
        });
        double dx = align == Align.CENTER ? -gl.width() / 2.0 : align == Align.RIGHT ? -gl.width() : 0;
        g.translate(x + dx, y);
        g.setColor(OUTLINE_COLOR);
        g.fill(gl.outline());
        g.setColor(fill);
        g.fill(gl.shape());
        g.translate(-(x + dx), -y);
    }

    public static void panel(Graphics2D g, double x, double y, double w, double h) {
        int r = (int) Math.max(6, h * 0.08);
        g.setColor(PANEL);
        g.fillRoundRect((int) x, (int) y, (int) w, (int) h, r, r);
        g.setColor(new Color(255, 255, 255, 30));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect((int) x, (int) y, (int) w, (int) h, r, r);
    }

    public static void bar(Graphics2D g, double x, double y, double w, double h, double value, Color c) {
        g.setColor(new Color(255, 255, 255, 35));
        g.fillRect((int) x, (int) y, (int) w, (int) h);
        g.setColor(c);
        g.fillRect((int) x, (int) y, (int) (w * Math.max(0, Math.min(1, value))), (int) h);
    }

    /** Fondo animado para menús: degradé + franjas diagonales. */
    public static void menuBackground(Graphics2D g, int w, int h, double time) {
        g.setPaint(new GradientPaint(0, 0, new Color(0x0B1430), 0, h, new Color(0x2A0E3A)));
        g.fillRect(0, 0, w, h);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g.setColor(ACCENT);
        int step = (int) (h * 0.18);
        int off = (int) ((time * 60) % step);
        for (int x = -h + off; x < w + h; x += step) {
            g.fillPolygon(new int[]{x, x + step / 3, x + step / 3 + h, x + h}, new int[]{0, 0, h, h}, 4);
        }
        g.setComposite(old);
        g.setPaint(new GradientPaint(0, h * 0.75f, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 160)));
        g.fillRect(0, (int) (h * 0.75), w, h);
    }

    /**
     * Logo del juego: letras itálicas con degradé amarillo-rojo, volumen extruido y
     * líneas de velocidad (diseño propio, dibujado por código).
     */
    public static void logo(Graphics2D g, int w, double y, double size) {
        java.awt.geom.AffineTransform italic = java.awt.geom.AffineTransform.getShearInstance(-0.22, 0);
        Font f = display((float) size).deriveFont(italic);
        java.awt.font.GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), "DAYTONA");
        java.awt.Rectangle bounds = gv.getPixelBounds(g.getFontRenderContext(), 0, 0);
        double x = w / 2.0 - bounds.getCenterX();
        java.awt.Shape text = gv.getOutline((float) x, (float) y);
        double top = y + bounds.y;
        double height = bounds.height;

        // líneas de velocidad
        Composite old = g.getComposite();
        for (int i = 0; i < 7; i++) {
            double ly = top + height * (0.15 + i * 0.12);
            double len = w * (0.25 + 0.06 * (i % 3));
            g.setPaint(new GradientPaint((float) (w / 2.0 - len), 0, new Color(255, 120, 0, 0), (float) (w / 2.0), 0,
                    new Color(255, 60, 0, 170)));
            g.fillRect((int) (w / 2.0 - len - bounds.width * 0.42), (int) ly, (int) len, Math.max(2, (int) (height * 0.05)));
        }
        g.setComposite(old);

        // extrusión
        int depth = Math.max(3, (int) (size / 14));
        for (int d = depth; d > 0; d--) {
            g.translate(d, d);
            g.setColor(new Color(0x5A0600));
            g.fill(text);
            g.translate(-d, -d);
        }
        g.setStroke(new BasicStroke((float) Math.max(3, size / 12), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x0B0F2E));
        g.draw(text);
        g.setPaint(new LinearGradientPaint(0, (float) top, 0, (float) (top + height),
                new float[]{0f, 0.45f, 0.55f, 1f},
                new Color[]{new Color(0xFFF6A0), new Color(0xFFC21A), new Color(0xFF8A00), new Color(0xE01E10)}));
        g.fill(text);
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke((float) Math.max(1, size / 40)));
        g.draw(text);

        // banda inferior
        Font sub = display((float) (size * 0.24)).deriveFont(italic);
        FontMetrics fm = g.getFontMetrics(sub);
        String subtitle = "ARCADE  RACING";
        double bw = fm.stringWidth(subtitle) + size * 0.8;
        double bh = size * 0.34;
        double bx = w / 2.0 - bw / 2;
        double by = y + size * 0.16;
        int[] px = {(int) (bx + bh * 0.4), (int) (bx + bw + bh * 0.4), (int) (bx + bw), (int) bx};
        int[] py = {(int) by, (int) by, (int) (by + bh), (int) (by + bh)};
        g.setColor(new Color(0x0B0F2E));
        g.fillPolygon(px, py, 4);
        for (int k = 0; k < 4; k++) {
            px[k] -= (int) (size * 0.03);
            py[k] -= (int) (size * 0.03);
        }
        g.setColor(new Color(0xD7261E));
        g.fillPolygon(px, py, 4);
        text(g, subtitle, w / 2.0, by + bh * 0.78 - size * 0.03, sub, TEXT, Align.CENTER);
    }

    public static String formatTime(double seconds) {
        if (seconds < 0) {
            return "--:--.---";
        }
        long ms = Math.round(seconds * 1000);
        return String.format("%d:%02d.%03d", ms / 60000, (ms / 1000) % 60, ms % 1000);
    }

    public static String ordinal(int n) {
        return n + "º";
    }

    /** Escribe un texto partiéndolo en renglones de ancho máximo {@code maxW}. */
    public static void wrapText(Graphics2D g, String text, double x, double y, double maxW, double s) {
        g.setFont(body((float) (15 * s)));
        StringBuilder line = new StringBuilder();
        double ly = y;
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(test) > maxW && line.length() > 0) {
                text(g, line.toString(), x, ly, body((float) (15 * s)), TEXT, Align.LEFT);
                ly += 22 * s;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        text(g, line.toString(), x, ly, body((float) (15 * s)), TEXT, Align.LEFT);
    }
}
