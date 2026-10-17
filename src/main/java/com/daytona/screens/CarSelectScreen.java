package com.daytona.screens;

import com.daytona.domain.car.CarSpec;
import com.daytona.domain.car.TransmissionType;
import com.daytona.input.ControlMode;
import com.daytona.input.ShifterType;
import com.daytona.render.UiKit;
import com.daytona.screens.ui.MenuList;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/** Elección de auto, caja y modo de control antes de correr. */
public final class CarSelectScreen extends MenuScreen {

    private final RaceSession session;
    private int carIndex;

    public CarSelectScreen(GameContext ctx, RaceSession session) {
        super(ctx);
        this.session = session;
        this.carIndex = ctx.cars().indexOf(session.carId());
    }

    @Override
    protected String title() {
        return "ELEGÍ TU AUTO";
    }

    @Override
    protected double listWidth() {
        return 0.42;
    }

    @Override
    protected void buildItems(MenuList list) {
        List<CarSpec> cars = ctx.cars().playable();
        list.add(MenuList.choice("Auto", () -> cars.get(carIndex).name(), d -> carIndex = Math.floorMod(carIndex + d, cars.size())));
        list.add(MenuList.choice("Transmisión", () -> ctx.settings().transmission().label(), d -> {
            TransmissionType[] v = TransmissionType.values();
            ctx.settings().setTransmission(v[Math.floorMod(ctx.settings().transmission().ordinal() + d, v.length)]);
        }));
        list.add(MenuList.choice("Palanca", () -> ctx.settings().shifter().label(), d -> {
            ShifterType[] v = ShifterType.values();
            ctx.settings().setShifter(v[Math.floorMod(ctx.settings().shifter().ordinal() + d, v.length)]);
        }, () -> ctx.settings().transmission() == TransmissionType.MANUAL));
        list.add(MenuList.choice("Control", () -> ctx.settings().controlMode().label(), d -> {
            ControlMode[] v = ControlMode.values();
            ctx.settings().setControlMode(v[Math.floorMod(ctx.settings().controlMode().ordinal() + d, v.length)]);
            ctx.settings().setDeviceKey("");
            ctx.input().refreshSource();
        }));
        list.add(MenuList.action("Configurar controles…", () -> ctx.nav().controls(this)));
        list.add(MenuList.action(session.isChampionship() ? "¡A correr el campeonato!" : "Continuar", this::next));
    }

    private void next() {
        CarSpec car = ctx.cars().playable().get(carIndex);
        session.setCarId(car.id());
        ctx.settings().setLastCar(car.id());
        if (session.isChampionship()) {
            ctx.nav().race(session);
        } else {
            ctx.nav().trackSelect(session);
        }
    }

    @Override
    protected void onBack() {
        ctx.nav().mainMenu();
    }

    @Override
    protected void renderExtra(Graphics2D g, int w, int h, double s) {
        CarSpec car = ctx.cars().playable().get(carIndex);
        double px = w * 0.53;
        double pw = w * 0.42;
        UiKit.panel(g, px, 86 * s, pw, h - 130 * s);
        double yaw = Math.sin(time * 1.2) * 0.9;
        BufferedImage img = ctx.atlas().car(car, yaw, time % 3 < 0.4);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        double iw = pw * 0.8;
        double ih = iw * img.getHeight() / img.getWidth();
        g.setColor(new Color(255, 255, 255, 18));
        g.fillOval((int) (px + pw * 0.1), (int) (100 * s + ih * 0.8), (int) iw, (int) (ih * 0.35));
        g.drawImage(img, (int) (px + (pw - iw) / 2), (int) (100 * s), (int) iw, (int) ih, null);
        double y = 110 * s + ih;
        UiKit.shadowText(g, car.name().toUpperCase() + "  #" + car.number(), px + 20 * s, y, UiKit.display((float) (26 * s)),
                UiKit.ACCENT, UiKit.Align.LEFT);
        UiKit.text(g, car.tagline(), px + 20 * s, y + 22 * s, UiKit.body((float) (13 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        y += 44 * s;
        stat(g, "Velocidad", car.topSpeedKmh(), 290, 350, px, y, pw, s,
                Math.round(ctx.settings().speedUnit().convert(car.topSpeedKmh())) + " " + ctx.settings().speedUnit().label());
        stat(g, "Aceleración", car.accel(), 0.85, 1.2, px, y + 24 * s, pw, s, null);
        stat(g, "Agarre", car.grip(), 0.85, 1.15, px, y + 48 * s, pw, s, null);
        stat(g, "Manejo", car.handling(), 0.85, 1.15, px, y + 72 * s, pw, s, null);
        stat(g, "Frenos", car.braking(), 0.9, 1.15, px, y + 96 * s, pw, s, null);
        UiKit.text(g, car.gears() + " marchas", px + 20 * s, y + 124 * s, UiKit.bodyBold((float) (13 * s)), UiKit.MUTED,
                UiKit.Align.LEFT);
        UiKit.text(g, "Control: " + ctx.input().sourceDescription(), px + 20 * s, y + 144 * s, UiKit.body((float) (12 * s)),
                ctx.input().usingFallback() ? UiKit.BAD : UiKit.MUTED, UiKit.Align.LEFT);
    }

    private static void stat(Graphics2D g, String label, double v, double min, double max, double px, double y, double pw,
                             double s, String extra) {
        UiKit.text(g, label, px + 20 * s, y, UiKit.bodyBold((float) (13 * s)), UiKit.TEXT, UiKit.Align.LEFT);
        double r = Math.max(0.05, Math.min(1, (v - min) / (max - min)));
        UiKit.bar(g, px + 120 * s, y - 10 * s, pw * 0.5, 10 * s, r, UiKit.ACCENT);
        if (extra != null) {
            UiKit.text(g, extra, px + pw - 16 * s, y, UiKit.bodyBold((float) (12 * s)), UiKit.MUTED, UiKit.Align.RIGHT);
        }
    }
}
