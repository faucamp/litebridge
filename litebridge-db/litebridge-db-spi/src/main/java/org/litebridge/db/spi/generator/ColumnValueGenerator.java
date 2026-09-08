package org.litebridge.db.spi.generator;

/**
 * A functional interface for generating values for a specific database column.
 * <p>
 * Implementations of this interface should provide a mechanism to compute or fetch the value to be assigned to
 * the column.
 * <p>
 * The {@code ColumnValueGenerator} interface is typically used in scenarios where
 * custom or dynamic values need to be generated for database expressions during operations like
 * data insertion or updates.
 */
@FunctionalInterface
public interface ColumnValueGenerator {

    /**
     * Generates a value for a database column.
     *
     * @return the generated value for the column.
     */
    String generate();

}
