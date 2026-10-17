package com.daytona.screens.ui;

import com.daytona.input.InputManager;
import com.daytona.input.MenuAction;
import com.daytona.render.UiKit;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Lista de opciones navegable con cualquier dispositivo (componente reutilizable). */
public final class MenuList {

    /** Opción de menú (patrón Command: cada ítem encapsula su acción). */
    public interface Item {
        String label();

        default String value() {
            return null;
        }

        default void confirm() {
        }

        default void adjust(int direction) {
        }

        default boolean enabled() {
            return true;
        }
    }

    public static Item action(String label, Runnable action) {
        return new Item() {
            @Override public String label() { return label; }
            @Override public void confirm() { action.run(); }
        };
    }

    public static Item action(String label, Runnable action, BooleanSupplier enabled) {
        return new Item() {
            @Override public String label() { return label; }
            @Override public void confirm() { action.run(); }
            @Override public boolean enabled() { return enabled.getAsBoolean(); }
        };
    }

    public static Item choice(String label, Supplier<String> value, IntConsumer adjust) {
        return new Item() {
            @Override public String label() { return label; }
            @Override public String value() { return value.get(); }
            @Override public void confirm() { adjust.accept(1); }
            @Override public void adjust(int d) { adjust.accept(d); }
        };
    }

    public static Item choice(String label, Supplier<String> value, IntConsumer adjust, BooleanSupplier enabled) {
        return new Item() {
            @Override public String label() { return label; }
            @Override public String value() { return value.get(); }
            @Override public void confirm() { adjust.accept(1); }
            @Override public void adjust(int d) { adjust.accept(d); }
            @Override public boolean enabled() { return enabled.getAsBoolean(); }
        };
    }

    private final List<Item> items = new ArrayList<>();
    private int selected;
    private double flash;

    public MenuList add(Item item) {
        items.add(item);
        return this;
    }

    public void clear() {
        items.clear();
        selected = 0;
    }

    public int selected() {
        return selected;
    }

    public void select(int i) {
        selected = Math.max(0, Math.min(items.size() - 1, i));
    }

    public int size() {
        return items.size();
    }

    /** @return true si hubo navegación o acción (para sonido de menú) */
    public boolean update(InputManager in, double dt) {
        flash += dt;
        if (items.isEmpty()) {
            return false;
        }
        if (in.pressed(MenuAction.UP)) {
            move(-1);
            return true;
        }
        if (in.pressed(MenuAction.DOWN)) {
            move(1);
            return true;
        }
        Item it = items.get(selected);
        if (!it.enabled()) {
            return false;
        }
        if (in.pressed(MenuAction.LEFT)) {
            it.adjust(-1);
            return true;
        }
        if (in.pressed(MenuAction.RIGHT)) {
            it.adjust(1);
            return true;
        }
        if (in.pressed(MenuAction.CONFIRM)) {
            it.confirm();
            return true;
        }
        return false;
    }

    private void move(int d) {
        int n = items.size();
        for (int k = 0; k < n; k++) {
            selected = Math.floorMod(selected + d, n);
            if (items.get(selected).enabled()) {
                return;
            }
        }
    }

    public void render(Graphics2D g, double x, double y, double w, double rowH, double s) {
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            double ry = y + i * rowH;
            boolean sel = i == selected;
            if (sel) {
                g.setPaint(new GradientPaint((float) x, 0, new Color(255, 194, 26, 200), (float) (x + w), 0, new Color(255, 77, 46, 60)));
                g.fillRoundRect((int) x, (int) ry, (int) w, (int) (rowH * 0.86), (int) (rowH * 0.3), (int) (rowH * 0.3));
            }
            Color c = !it.enabled() ? new Color(255, 255, 255, 70) : sel ? new Color(0x111111) : UiKit.TEXT;
            float fs = (float) (rowH * 0.46);
            double ty = ry + rowH * 0.58;
            UiKit.text(g, it.label(), x + 14 * s, ty, UiKit.bodyBold(fs), c, UiKit.Align.LEFT);
            String v = it.value();
            if (v != null) {
                String shown = sel ? "‹  " + v + "  ›" : v;
                UiKit.text(g, shown, x + w - 14 * s, ty, UiKit.bodyBold(fs), sel ? new Color(0x111111) : UiKit.ACCENT,
                        UiKit.Align.RIGHT);
            }
        }
    }
}
