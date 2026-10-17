package com.daytona.render;

import com.daytona.domain.car.CarSpec;

/** Datos mínimos para dibujar un auto en este frame. */
public final class CarSprite {
    private final double z;
    private final double x;
    private final CarSpec spec;
    private final double yaw;
    private final boolean braking;
    private final float alpha;
    private final boolean player;

    public CarSprite(double z, double x, CarSpec spec, double yaw, boolean braking, float alpha, boolean player) {
        this.z = z;
        this.x = x;
        this.spec = spec;
        this.yaw = yaw;
        this.braking = braking;
        this.alpha = alpha;
        this.player = player;
    }

    public double z() {
        return z;
    }

    public double x() {
        return x;
    }

    public CarSpec spec() {
        return spec;
    }

    public double yaw() {
        return yaw;
    }

    public boolean braking() {
        return braking;
    }

    public float alpha() {
        return alpha;
    }

    public boolean player() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarSprite)) {
            return false;
        }
        CarSprite other = (CarSprite) o;
        return java.util.Objects.equals(z, other.z) && java.util.Objects.equals(x, other.x) && java.util.Objects.equals(spec, other.spec) && java.util.Objects.equals(yaw, other.yaw) && java.util.Objects.equals(braking, other.braking) && java.util.Objects.equals(alpha, other.alpha) && java.util.Objects.equals(player, other.player);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(z, x, spec, yaw, braking, alpha, player);
    }
}
