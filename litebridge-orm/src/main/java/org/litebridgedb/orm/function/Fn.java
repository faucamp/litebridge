package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

/**
 * Functions: Utility class that provides methods for constructing query expressions.
 * <p>
 * This class is a collection of static functions to create different types
 * of select expressions within a database query. This includes selecting DTO
 * fields, database columns, or counting rows in a query.
 * <p>
 * This class cannot be instantiated.
 */
public final class Fn {

    private Fn() {
    }

    /**
     * Selects a DTO field by name.
     * <p>
     * Shorthand for {@link #field(String)}.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectField} expression instance to select the specified field.
     */
    public static Expression f(final String field) {
        return new SelectField(field);
    }

    /**
     * Selects a DTO field by name.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectField} expression instance to select the specified field.
     */
    public static Expression field(final String field) {
        return f(field);
    }

    /**
     * Selects a database column by name.
     * <p>
     * This is shorthand for {@link #column(String)}.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression c(final String column) {
        return ca(column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression column(final String column) {
        return c(column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(String, String)}
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression c(final String table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression column(final String table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(Table, String)}
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression c(final Table table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression column(final Table table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)} (Table, String, String)}
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression ca(final Table table, final String column, final @Nullable String columnAlias) {
        return new SelectColumn(new Column(table, column, columnAlias));
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression columnAlias(final Table table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(String, String)}
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression ca(final String column, final @Nullable String alias) {
        return new ProtoSelectColumn(column, alias);
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
    public static Expression columnAlias(final String column, final @Nullable String alias) {
        return ca(column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)}
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression ca(final String table, final String column, final @Nullable String columnAlias) {
        return ca(new Table(table), column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoSelectColumn} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoSelectColumn} expression instance to select a specific column.
     */
    public static Expression columnAlias(final String table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    /**
     * {@code COUNT()}: Selects the count of rows matching the query.
     *
     * @return a {@link Count} expression instance to select the count of rows.
     */
    public static Count count() {
        return new Count();
    }
}
