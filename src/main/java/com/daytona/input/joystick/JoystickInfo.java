package com.daytona.input.joystick;

/**
 * Dispositivo detectado.
 *
 * @param key clave estable para guardar su perfil (backend:vendor:producto o nombre)
 */
public final class JoystickInfo {
    private final String key;
    private final String name;
    private final Source source;
    private final int nativeId;
    private final int vendorId;
    private final int productId;
    private final int axes;
    private final int buttons;

    public JoystickInfo(String key, String name, Source source, int nativeId, int vendorId, int productId, int axes, int buttons) {
        this.key = key;
        this.name = name;
        this.source = source;
        this.nativeId = nativeId;
        this.vendorId = vendorId;
        this.productId = productId;
        this.axes = axes;
        this.buttons = buttons;
    }

    public String key() {
        return key;
    }

    public String name() {
        return name;
    }

    public Source source() {
        return source;
    }

    public int nativeId() {
        return nativeId;
    }

    public int vendorId() {
        return vendorId;
    }

    public int productId() {
        return productId;
    }

    public int axes() {
        return axes;
    }

    public int buttons() {
        return buttons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JoystickInfo)) {
            return false;
        }
        JoystickInfo other = (JoystickInfo) o;
        return java.util.Objects.equals(key, other.key) && java.util.Objects.equals(name, other.name) && java.util.Objects.equals(source, other.source) && java.util.Objects.equals(nativeId, other.nativeId) && java.util.Objects.equals(vendorId, other.vendorId) && java.util.Objects.equals(productId, other.productId) && java.util.Objects.equals(axes, other.axes) && java.util.Objects.equals(buttons, other.buttons);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, name, source, nativeId, vendorId, productId, axes, buttons);
    }
    public enum Source { XINPUT, WINMM, LINUX }

    private static final int LOGITECH = 0x046D;
    private static final int THRUSTMASTER = 0x044F;
    private static final int FANATEC = 0x0EB7;

    /** Heurística para preseleccionar volantes. */
    public boolean likelyWheel() {
        String n = name.toLowerCase();
        if (n.contains("wheel") || n.contains("volante") || n.contains("racing") || n.contains("driving")
                || n.contains("g29") || n.contains("g920") || n.contains("g923") || n.contains("g27")
                || n.contains("t300") || n.contains("t150") || n.contains("tmx") || n.contains("fanatec")) {
            return true;
        }
        return vendorId == FANATEC || vendorId == THRUSTMASTER
                || (vendorId == LOGITECH && (productId == 0xC24F || productId == 0xC262 || productId == 0xC266
                || productId == 0xC267 || productId == 0xC29B || productId == 0xC29A || productId == 0xC294
                || productId == 0xC298 || productId == 0xC299));
    }

    public boolean isLogitechWheel() {
        return vendorId == LOGITECH && likelyWheel();
    }

    public String label() {
        switch (source) {
            case XINPUT:
                return name + " [XInput]";
            case WINMM:
                return name + " [DirectInput]";
            case LINUX:
                return name;
            default:
                throw new IllegalStateException(String.valueOf(source));
        }
    }
}
