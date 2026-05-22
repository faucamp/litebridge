package org.litebridgedb.db.spi.generator;

import org.litebridgedb.db.spi.ColumnMetaData;

/**
 * A functional interface for generating values for a specific database column. Implementations
 * of this interface should provide a mechanism to compute or fetch the value to be assigned to
 * the column, potentially based on the column's metadata information.
 * <p>
 * The {@code ColumnValueGenerator} interface is typically used in scenarios where
 * custom or dynamic values need to be generated for database columns during operations like
 * data insertion or updates.
 * <p>
 * This interface enforces the implementation of a single method {@code generate}, which is expected
 * to return the computed value for the column.
 */
@FunctionalInterface
public interface ColumnValueGenerator {

    /**
     * Generates a value for a given database column based on its metadata.
     *
     * @param columnMetaData metadata information for the database column, such as its name, data type, size, and other attributes
     * @return the generated value for the column, which could be dynamically computed or fetched based on the column's metadata
     */
    Object generate(ColumnMetaData columnMetaData);

}
