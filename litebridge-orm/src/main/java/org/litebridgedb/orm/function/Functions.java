package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;

/**
 * Utility class that provides methods for constructing query expressions.
 * <p>
 * This class is a collection of static functions to create different types
 * of select expressions within a database query. This includes selecting DTO
 * fields, database columns, or counting rows in a query.
 * <p>
 * This class cannot be instantiated.
 */
public final class Functions {

    private Functions() {
    }

    /**
     * Selects a DTO field by name.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectField} expression instance to select the specified field.
     */
    public static Expression f(final String field) {
        return new SelectField(field);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression c(final String column) {
        return c(column, null);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression c(final String column, final @Nullable String alias) {
        return new ProtoSelectColumn(column, alias);
    }

    /**
     * {@code COUNT()}: Selects the count of rows matching the query.
     *
     * @return a {@link Count} expression instance to select the count of rows.
     */
    public static Expression count() {
        return new Count();
    }
}
