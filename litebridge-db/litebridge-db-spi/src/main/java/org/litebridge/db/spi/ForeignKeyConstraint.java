package org.litebridge.db.spi;

public record ForeignKeyConstraint(String name, Column foreignKey) {
}
