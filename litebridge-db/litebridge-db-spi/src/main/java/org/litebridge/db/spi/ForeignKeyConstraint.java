package org.litebridge.db.spi;

/**
 * A foreign key constraint between two columns.
 *
 * @param name       the name of the constraint
 * @param foreignKey the column that acts as the foreign key
 */
public record ForeignKeyConstraint(String name, Column foreignKey) {
}
