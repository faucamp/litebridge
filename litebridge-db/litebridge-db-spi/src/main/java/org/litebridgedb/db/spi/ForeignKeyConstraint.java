package org.litebridgedb.db.spi;

public record ForeignKeyConstraint(String name, Column foreignKey) {
}
