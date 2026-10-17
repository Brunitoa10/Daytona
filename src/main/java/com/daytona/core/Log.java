package com.daytona.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Registro mínimo sobre java.util.logging (disponible desde Java 8, sin dependencias). */
public final class Log {

    private final Logger logger;

    private Log(String name) {
        this.logger = Logger.getLogger("daytona." + name);
    }

    public static Log of(String name) {
        return new Log(name);
    }

    public void warn(String message) {
        logger.log(Level.WARNING, message);
    }

    public void warn(String message, Throwable error) {
        logger.log(Level.WARNING, message, error);
    }

    public void info(String message) {
        logger.log(Level.INFO, message);
    }
}
