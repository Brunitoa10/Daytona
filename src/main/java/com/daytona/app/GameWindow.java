package com.daytona.app;

import com.daytona.config.Resolution;
import com.daytona.config.Settings;
import com.daytona.input.KeyboardState;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.daytona.render.theme.PostProcessor;

/**
 * Ventana con render acelerado: se dibuja en una imagen volátil (VRAM) a la
 * resolución interna y se escala a la ventana con doble buffer, manteniendo 16:9.
 */
public final class GameWindow implements DisplayControl {

    private final Settings settings;
    private final Supplier<PostProcessor> postProcessor;
    private final JFrame frame;
    private final Canvas canvas;
    private VolatileImage back;
    private int width;
    private int height;
    private volatile boolean quit;
    private volatile double fps;
    private final boolean linux = System.getProperty("os.name", "").toLowerCase().contains("linux");

    public GameWindow(String title, Settings settings, KeyboardState keys, Supplier<PostProcessor> postProcessor) {
        this.settings = settings;
        this.postProcessor = postProcessor;
        Resolution r = settings.resolution();
        this.width = r.width();
        this.height = r.height();
        this.canvas = new Canvas();
        this.frame = new JFrame(title);
        runOnEdt(() -> {
            canvas.setIgnoreRepaint(true);
            canvas.setBackground(Color.BLACK);
            canvas.setFocusTraversalKeysEnabled(false);
            canvas.addKeyListener(keys);
            frame.addKeyListener(keys);
            frame.setIgnoreRepaint(true);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    quit = true;
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    keys.releaseAll();
                }
            });
            frame.add(canvas);
            configureFrame(settings.fullscreen());
        });
        canvas.createBufferStrategy(2);
    }

    private void configureFrame(boolean fullscreen) {
        frame.dispose();
        frame.setUndecorated(fullscreen);
        Rectangle screen = frame.getGraphicsConfiguration().getBounds();
        if (fullscreen) {
            frame.setBounds(screen);
        } else {
            int w = (int) (screen.width * 0.75);
            int h = w * 9 / 16;
            canvas.setPreferredSize(new Dimension(w, h));
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
        canvas.requestFocus();
    }

    private static void runOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /** Dibuja un frame: el pintor recibe el Graphics de la resolución interna. */
    public void present(Consumer<Graphics2D> painter) {
        GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
        if (gc == null) {
            return;
        }
        if (back == null || back.getWidth() != width || back.getHeight() != height) {
            back = gc.createCompatibleVolatileImage(width, height);
        }
        do {
            if (back.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                back = gc.createCompatibleVolatileImage(width, height);
            }
            Graphics2D g = back.createGraphics();
            try {
                painter.accept(g);
            } finally {
                g.dispose();
            }
        } while (back.contentsLost());

        PostProcessor post = postProcessor.get();
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(2);
            return;
        }
        try {
            do {
                do {
                    Graphics2D sg = (Graphics2D) bs.getDrawGraphics();
                    int cw = canvas.getWidth();
                    int ch = canvas.getHeight();
                    double scale = Math.min(cw / (double) width, ch / (double) height);
                    int dw = (int) Math.round(width * scale);
                    int dh = (int) Math.round(height * scale);
                    int ox = (cw - dw) / 2;
                    int oy = (ch - dh) / 2;
                    sg.setColor(Color.BLACK);
                    if (ox > 0) {
                        sg.fillRect(0, 0, ox, ch);
                        sg.fillRect(ox + dw, 0, cw - ox - dw, ch);
                    }
                    if (oy > 0) {
                        sg.fillRect(0, 0, cw, oy);
                        sg.fillRect(0, oy + dh, cw, ch - oy - dh);
                    }
                    sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, post.interpolation());
                    sg.drawImage(back, ox, oy, dw, dh, null);
                    post.overlay(sg, ox, oy, dw, dh, height);
                    sg.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        } catch (IllegalStateException e) {
            canvas.createBufferStrategy(2); // la ventana se reconfiguró
        }
        if (linux) {
            Toolkit.getDefaultToolkit().sync();
        }
    }

    @Override
    public void applyVideoSettings() {
        Resolution r = settings.resolution();
        width = r.width();
        height = r.height();
        back = null;
    }

    @Override
    public void toggleFullscreen() {
        boolean fs = !settings.fullscreen();
        settings.setFullscreen(fs);
        runOnEdt(() -> configureFrame(fs));
        canvas.createBufferStrategy(2);
        back = null;
    }

    @Override
    public void requestQuit() {
        quit = true;
    }

    public boolean quitRequested() {
        return quit;
    }

    public void setFps(double fps) {
        this.fps = fps;
    }

    @Override
    public double fps() {
        return fps;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    public void dispose() {
        runOnEdt(frame::dispose);
    }
}
