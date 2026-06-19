package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumn;
import org.litebridgedb.orm.expression.select.SelectField;

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

    // Field/column selectors

    /**
     * Selects a DTO field by name.
     * <p>
     * Shorthand for {@link #field(String)}.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectField} expression instance to select the specified field.
     */
    public static Expression f(final String field) {
        return new ProtoColumnExpression(SelectField.class, field, null);
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
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression c(final String column) {
        return ca(column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression column(final String column) {
        return c(column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(String, String)}
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression c(final String table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression column(final String table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(Table, String)}
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression c(final Table table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression column(final Table table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)} (Table, String, String)}
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression ca(final Table table, final String column, final @Nullable String columnAlias) {
        return new SelectColumn(new Column(table, column, columnAlias));
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression columnAlias(final Table table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(String, String)}
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression ca(final String column, final @Nullable String alias) {
        return new ProtoColumnExpression(SelectColumn.class, column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression columnAlias(final String column, final @Nullable String alias) {
        return ca(column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)}
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression ca(final String table, final String column, final @Nullable String columnAlias) {
        return ca(new Table(table), column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpression} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoColumnExpression} expression instance to select a specific column.
     */
    public static Expression columnAlias(final String table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    // SQL aggregate functions

    /**
     * {@code AVG()}: Returns the average value of a column/field.
     *
     * @param column Name of the target column/field to calculate the average value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the average value of a column/field.
     */
    public static TypeOverrideExpression<Number> avg(final String column) {
        return avg(column, Number.class);
    }

    /**
     * {@code AVG()}: Returns the average value of a column/field.
     *
     * @param column Name of the target column/field to calculate the average value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the average value of a column/field.
     */
    public static <T extends Number> TypeOverrideExpression<T> avg(final String column, final Class<T> returnType) {
        return new ProtoNestableTOExpr<>(returnType, AvgSpec.class, column, null);
    }

    /**
     * {@code COUNT()}: Selects the count of rows matching the query.
     *
     * @return a {@link CountSpec} expression instance to select the count of rows.
     */
    public static TypeOverrideExpression<Long> count() {
        return new CountSpec();
    }

    // SQL scalar functions

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> upper(final String column) {
        return new ProtoNestableTOExpr<>(String.class, UpperSpec.class, column, null);
    }

    public static ProtoNestableTOExpr<String> upper(final ProtoExpression expression) {
        return new ProtoNestableTOExpr<>(String.class, UpperSpec.class, expression, null);
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> lower(final String column) {
        return new ProtoNestableTOExpr<>(String.class, LowerSpec.class, column, null);
    }

    public static ProtoNestableTOExpr<String> lower(final ProtoExpression expression) {
        return new ProtoNestableTOExpr<>(String.class, LowerSpec.class, expression, null);
    }

    /**
     * {@code SUBSTRING()}: Returns the lowercase value of a column's text.
     * <p>
     * This shorthand version omits the "length" parameter and thus
     * extracts everything from the start position to the end of the text.
     *
     * @param column Target column to extract characters from.
     * @param start  The starting position. The first character of a database string is always 1.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     * @see #substring(String, int, int)
     */
    public static ProtoNestableTOExpr<String> substring(final String column, final int start) {
        return new ProtoNestableTOExpr<>(String.class, SubstringSpec.class, column, null, new Object[]{start, null});
    }

    /**
     * {@code SUBSTRING()}: Returns the lowercase value of a column's text.
     *
     * @param column Target column to extract characters from.
     * @param start  The starting position. The first character of a database string is always 1.
     * @param length The number of characters to return.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     * @see #substring(String, int)
     */
    public static ProtoNestableTOExpr<String> substring(final String column, final int start, final int length) {
        return new ProtoNestableTOExpr<>(String.class, SubstringSpec.class, column, null, new Object[]{start, length});
    }
}
