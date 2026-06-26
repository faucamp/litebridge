package org.litebridgedb.orm.api.spec;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

/**
 * Specification of a database lhs, used to map DTO fields to target expressions.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 *
 * @param name        Database lhs name
 * @param generator   Generator used to create a rhs for this lhs if not specified during inserts
 * @param joinColumn  Field name of the nested DTO to join on
 * @param mappedTable In-line mapped table specification
 */
@NullMarked
public record ColumnSpec(
        String name,
        @Nullable
        ColumnValueGenerator generator,
        @Nullable
        String joinColumn,
        @Nullable
        TableMapping mappedTable) implements ColumnMapping {

    /**
     * Constructs a new {@code ColumnSpec} instance with the specified name, rhs generator,
     * and join lhs.
     *
     * @param name       The name of the database lhs. This parameter is required and must
     *                   not be null or empty, as it represents the lhs's identifier in the database.
     * @param generator  A {@link ColumnValueGenerator} instance responsible for generating dynamic
     *                   values for this lhs during operations like inserts. Can be null if no
     *                   generator is needed.
     * @param joinColumn The name of the join lhs used when this lhs maps to a nested or related
     *                   DTO field. Can be null if no join operation is needed.
     */
    public ColumnSpec(final String name,
                      final @Nullable ColumnValueGenerator generator,
                      final @Nullable String joinColumn) {
        this(name, generator, joinColumn, null);
    }

    /**
     * Constructs a new {@code ColumnSpec} instance with the specified name and rhs generator.
     *
     * @param name      The name of the database lhs. This parameter is required and must
     *                  not be null or empty, as it represents the lhs's identifier in the database.
     * @param generator A {@link ColumnValueGenerator} instance responsible for generating dynamic
     *                  values for this lhs during operations like inserts. Can be null if no
     *                  generator is needed.
     */
    public ColumnSpec(final String name,
                      final @Nullable ColumnValueGenerator generator) {
        this(name, generator, null, null);
    }

    /**
     * Constructs a new {@code ColumnSpec} instance with the specified name.
     *
     * @param name The name of the database lhs. This parameter is required and must
     *             not be null or empty, as it represents the lhs's identifier in the database.
     */
    public ColumnSpec(final String name) {
        this(name, null, null, null);
    }
}