package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridge.orm.expression.function.scalar.AbsSpec;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;

/**
 * Functions: Utility class that provides static methods for constructing query expressions.
 * <p>
 * This class is a collection of static functions to create different types
 * of select expressions within a database query. This includes selecting DTO
 * fields, database expressions, or counting rows in a query.
 * <p>
 * This class cannot be instantiated.
 */
public final class Fn {

    private Fn() {
    }

    // Field/column selectors

    /**
     * Selects a DTO field by name for the DTO class selected in the query.
     * <p>
     * Shorthand for {@link #field(String)}.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectFieldSpec} expression instance to select the specified field.
     */
    public static ExpressionSpec f(final String field) {
        return new ProtoColumnExpressionSpec(SelectFieldSpec.class, field, null);
    }

    /**
     * Selects a DTO field by name for the specified DTO type that is selected/joined in the query.
     * <p>
     * Shorthand for {@link #field(Class, String)}.
     *
     * @param dtoClass The DTO class.
     * @param field    The name of the DTO field to select.
     * @return a {@link SelectFieldSpec} expression instance to select the specified field.
     */
    public static ExpressionSpec f(final Class<?> dtoClass, final String field) {
        return new ProtoColumnExpressionSpec(SelectFieldSpec.class, field, null, new Object[]{dtoClass});
    }

    /**
     * Selects a DTO field by name for the DTO class selected in the query.
     *
     * @param field The name of the DTO field to select.
     * @return a {@link SelectFieldSpec} expression instance to select the specified field.
     */
    public static ExpressionSpec field(final String field) {
        return f(field);
    }

    /**
     * Selects a DTO field by name for the specified DTO type that is selected/joined in the query.
     *
     * @param dtoClass The DTO class.
     * @param field    The name of the DTO field to select.
     * @return a {@link SelectFieldSpec} expression instance to select the specified field.
     */
    public static ExpressionSpec field(final Class<?> dtoClass, final String field) {
        return f(dtoClass, field);
    }

    /**
     * Selects a database column by name.
     * <p>
     * This is shorthand for {@link #column(String)}.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec c(final String column) {
        return ca(column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec column(final String column) {
        return c(column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(String, String)}
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec c(final String table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec column(final String table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Shorthand for {@link #column(Table, String)}
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec c(final Table table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec column(final Table table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)} (Table, String, String)}
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec ca(final Table table, final String column, final @Nullable String columnAlias) {
        return new SelectColumnSpec(new Column(table, column, columnAlias));
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec columnAlias(final Table table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(String, String)}
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec ca(final String column, final @Nullable String alias) {
        return new ProtoColumnExpressionSpec(SelectColumnSpec.class, column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec columnAlias(final String column, final @Nullable String alias) {
        return ca(column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #columnAlias(Table, String, String)}
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec ca(final String table, final String column, final @Nullable String columnAlias) {
        return ca(new Table(table), column, columnAlias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * The returned {@link ProtoColumnExpressionSpec} value has no context of the table it is selecting from yet.
     *
     * @param table       The table to select the column from.
     * @param column      The name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a {@link ProtoColumnExpressionSpec} expression instance to select a specific column.
     */
    public static ExpressionSpec columnAlias(final String table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    // Java helper functions

    /**
     * Converts a database result into the specified Java type.
     * <p>
     * This uses Litebridge's registered type converter to perform the conversion;
     * it is not a database operation.
     * <p>
     * It can be used to ensure that the return value of a nested expression is converted to the specified Java type
     * on the ORM side; e.g. {@link #avg(ExpressionSpec)} returns a @{Number} instance by default,
     * with the actual return type being determined by the database. To convert the return type to a {@code Long},
     * {@code convert()} can be used to convert it before returning:
     * <code>
     * litebridge.select(Fn.convert(Fn.avg(column), Long.class));
     * </code>
     *
     * @param <T>        The type to convert the expression result to
     * @param expression The target expression result to convert
     * @param returnType The type to convert the expression result to
     * @return a {@link ProtoColumnExpressionSpec} expression instance to convert the return value of the nested expression
     */
    public static <T> ConvertSpec<T> convert(final ExpressionSpec expression, final Class<T> returnType) {
        return new ConvertSpec<>(expression, returnType);
    }

    /**
     * Converts the results of multiple expressions into a single Java object of the specified type.
     *
     * @param <T>         The target type.
     * @param returnType  The target type class.
     * @param expressions The expressions to convert.
     * @return a {@link ConvertIntent} instance.
     */
    public static <T> ConvertIntent<T> convert(final Class<T> returnType, final ExpressionSpec... expressions) {
        return new ConvertIntent<>(expressions, returnType);
    }

    /**
     * Converts the results of multiple expressions into a single database {@link Row} object.
     *
     * @param expressions The expressions to convert.
     * @return a {@link ConvertIntent} instance for {@link Row}.
     */
    public static ConvertIntent<Row> row(final ExpressionSpec... expressions) {
        return convert(Row.class, expressions);
    }
    // SQL aggregate functions

    /**
     * {@code AVG()}: Returns the average value of a column/field.
     *
     * @param column Name of the target column/field to calculate the average value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the average value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> avg(final String column) {
        return new ProtoNestableTOExpr<>(Number.class, AvgSpec.class, column, null);
    }

    /**
     * {@code AVG()}: Returns the average value of a column/field.
     *
     * @param expressionSpec Target nested expression to calculate the average value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the average value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> avg(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(Number.class, AvgSpec.class, expressionSpec, null);
    }

    /**
     * {@code MAX()}: Returns the highest or largest value within a specified column/field.
     *
     * @param column Name of the target column/field to calculate the maximum value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> max(final String column) {
        return new ProtoNestableTOExpr<>(Number.class, MaxSpec.class, column, null);
    }

    /**
     * {@code MAX()}: Returns the highest or largest value within a specified expression.
     *
     * @param expressionSpec Target nested expression to calculate the maximum value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> max(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(Number.class, MaxSpec.class, expressionSpec, null);
    }

    /**
     * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
     *
     * @param column Name of the target column/field to calculate the maximum value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> min(final String column) {
        return new ProtoNestableTOExpr<>(Number.class, MinSpec.class, column, null);
    }

    /**
     * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
     *
     * @param expressionSpec Target nested expression to calculate the maximum value of.
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a column/field.
     */
    public static TypeOverrideExpressionSpec<Number> min(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(Number.class, MinSpec.class, expressionSpec, null);
    }

    /**
     * {@code COUNT()}: Selects the count of rows matching the query.
     *
     * @return a {@link CountSpec} expression instance to select the count of rows.
     */
    public static TypeOverrideExpressionSpec<Long> count() {
        return new CountSpec();
    }

    // SQL scalar functions

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @param column Target column/field name
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> upper(final String column) {
        return new ProtoNestableTOExpr<>(String.class, UpperSpec.class, column, null);
    }

    /**
     * {@code UPPER()}: Returns the uppercase value of a column's text.
     *
     * @param expressionSpec Target nested expression
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> upper(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(String.class, UpperSpec.class, expressionSpec, null);
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @param column Target column/field name
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> lower(final String column) {
        return new ProtoNestableTOExpr<>(String.class, LowerSpec.class, column, null);
    }

    /**
     * {@code LOWER()}: Returns the lowercase value of a column's text.
     *
     * @param expressionSpec Target nested expression
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<String> lower(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(String.class, LowerSpec.class, expressionSpec, null);
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
        return new ProtoNestableTOExpr<>(String.class, SubstringSpec.class, column, null, new @Nullable Object[]{start, null});
    }

    /**
     * {@code SUBSTRING()}: Returns the lowercase value of a column's text.
     * <p>
     * This shorthand version omits the "length" parameter and thus
     * extracts everything from the start position to the end of the text.
     *
     * @param expressionSpec Target nested expression to extract characters from.
     * @param start          The starting position. The first character of a database string is always 1.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     * @see #substring(String, int, int)
     */
    public static ProtoNestableTOExpr<String> substring(final ExpressionSpec expressionSpec, final int start) {
        return new ProtoNestableTOExpr<>(String.class, SubstringSpec.class, expressionSpec, null, new @Nullable Object[]{start, null});
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

    /**
     * {@code SUBSTRING()}: Returns the lowercase value of a column's text.
     *
     * @param expressionSpec Target nested expression to extract characters from.
     * @param start          The starting position. The first character of a database string is always 1.
     * @param length         The number of characters to return.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     * @see #substring(String, int)
     */
    public static ProtoNestableTOExpr<String> substring(final ExpressionSpec expressionSpec, final int start, final int length) {
        return new ProtoNestableTOExpr<>(String.class, SubstringSpec.class, expressionSpec, null, new Object[]{start, length});
    }

    /**
     * {@code ABS()}: Absolute value of a number.
     *
     * @param column Target column/field.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<Number> abs(final String column) {
        return new ProtoNestableTOExpr<>(Number.class, AbsSpec.class, column, null);
    }

    /**
     * {@code ABS()}: Absolute value of a number.
     *
     * @param expressionSpec Target nested expression.
     * @return a {@link ProtoNestableTOExpr} expression instance to select a specific column.
     */
    public static ProtoNestableTOExpr<Number> abs(final ExpressionSpec expressionSpec) {
        return new ProtoNestableTOExpr<>(Number.class, AbsSpec.class, expressionSpec, null);
    }

    /**
     * {@code CURRENT_TIMESTAMP}: Returns the current date and time.
     *
     * @return a {@link CurrentTimestampSpec} instance.
     */
    public static CurrentTimestampSpec currentTimestamp() {
        return new CurrentTimestampSpec();
    }
}
