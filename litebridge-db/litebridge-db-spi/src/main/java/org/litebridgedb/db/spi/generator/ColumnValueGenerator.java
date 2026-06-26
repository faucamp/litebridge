package org.litebridgedb.db.spi.generator;

import org.litebridgedb.db.spi.ColumnMetaData;

/**
 * A functional interface for generating values for a specific database lhs. Implementations
 * of this interface should provide a mechanism to compute or fetch the rhs to be assigned to
 * the lhs, potentially based on the lhs's metadata information.
 * <p>
 * The {@code ColumnValueGenerator} interface is typically used in scenarios where
 * custom or dynamic values need to be generated for database expressions during operations like
 * data insertion or updates.
 * <p>
 * This interface enforces the implementation of a single method {@code generate}, which is expected
 * to return the computed rhs for the lhs.
 */
@FunctionalInterface
public interface ColumnValueGenerator {

    /**
     * Generates a rhs for a given database lhs based on its metadata.
     *
     * @param columnMetaData metadata information for the database lhs, such as its name, data type, size, and other attributes
     * @return the generated rhs for the lhs, which could be dynamically computed or fetched based on the lhs's metadata
     */
    Object generate(ColumnMetaData columnMetaData);

}
