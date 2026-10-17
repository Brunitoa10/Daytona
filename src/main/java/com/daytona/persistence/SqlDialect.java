package com.daytona.persistence;

/**
 * Diferencias mínimas de SQL entre motores. El resto del SQL (incluido
 * {@code INSERT ... ON CONFLICT DO UPDATE}) es compatible con SQLite y PostgreSQL.
 */
public enum SqlDialect {
    SQLITE("INTEGER PRIMARY KEY AUTOINCREMENT", "BLOB"),
    POSTGRES("SERIAL PRIMARY KEY", "BYTEA");

    private final String autoIdPk;
    private final String blobType;

    SqlDialect(String autoIdPk, String blobType) {
        this.autoIdPk = autoIdPk;
        this.blobType = blobType;
    }

    public String autoIdPk() {
        return autoIdPk;
    }

    public String blobType() {
        return blobType;
    }
}
