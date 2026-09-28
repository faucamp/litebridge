package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.SelectTerminal;
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
import org.litebridge.orm.expression.select.AliasReferenceSpec;
import org.litebridge.orm.expression.select.DtoAliasSpec;
import org.litebridge.orm.expression.select.LiteralExpressionSpec;
import org.litebridge.orm.expression.select.QueryAliasSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SqlFromTargetSpec;
import org.litebridge.orm.expression.select.TableAliasSpec;

import java.util.List;
import java.util.function.Function;

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

    // Select targets (FROM/JOIN targets)

    /**
     * Aliases the specified entity/mapped DTO class.
     * <p>
     * The result can be used as a target for a `FROM` or `JOIN` clause.
     *
     * @param dtoClass The DTO class to alias.
     * @param alias    The alias to use for the DTO class.
     * @param <DTO>    The entity or mapped DTO class to alias.
     * @return a target specification for the specified DTO class and alias.
     */
    public static <DTO> DtoAliasSpec<DTO> alias(final Class<DTO> dtoClass, final String alias) {
        return new DtoAliasSpec<>(dtoClass, alias);
    }

    /**
     * Aliases the specified table.
     * <p>
     * The result can be used as a target for a `FROM` or `JOIN` clause.
     *
     * @param table Name of the table to alias.
     * @param alias The alias to use for the table.
     * @return a target specification for the specified table and alias.
     */
    public static SqlFromTargetSpec aliasTable(final String table, final String alias) {
        return new TableAliasSpec(table, alias);
    }

    /**
     * Aliases the specified subquery.
     * <p>
     * This allows the results from the subquery to be referenced in the parent query.
     * It can be used as a target for a `FROM` or `JOIN` clause.
     *
     * @param query The subquery to alias.
     * @param alias The alias to use for the subquery.
     * @return a target specification for the specified subquery and alias.
     */
    public static SqlFromTargetSpec alias(final Function<SelectApi, SelectTerminal<?>> query, final String alias) {
        return new QueryAliasSpec(query, alias);
    }

    // Field/column selectors

    /**
     * Selects a DTO field by name for the DTO class selected in the query.
     * <p>
     * Shorthand for {@link #field(String)}.
     * <p>
     * This infers the source entity/mapped DTO class from the available `FROM`/`JOIN` clauses.
     *
     * @param field The name of the DTO field to select.
     * @return a query expression selecting the target entity/DTO field
     * @see Fn#f(Class, String) to specify the parent DTO class explicitly to avoid potential ambiguity.
     */
    public static ExpressionSpec f(final String field) {
        return new ProtoColumnExpressionSpec(SelectColumnSpec.class, field, null);
    }

    /**
     * Selects a DTO field by name for the specified DTO type that is selected/joined in the query.
     * <p>
     * Shorthand for {@link #field(Class, String)}.
     *
     * @param dtoClass The DTO class to select from.
     * @param field    The name of the DTO field to select.
     * @return a query expression selecting the target entity/DTO field
     */
    public static ExpressionSpec f(final Class<?> dtoClass, final String field) {
        return new ProtoColumnExpressionSpec(SelectColumnSpec.class, field, null, new Object[]{dtoClass});
    }

    /**
     * Selects a DTO field by name for the entity/DTO class selected in the query.
     * <p>
     * This infers the source entity/mapped DTO class from the available `FROM`/`JOIN` clauses.
     *
     * @param field The name of the DTO field to select.
     * @return a query expression selecting the target entity/DTO field
     * @see Fn#f(String) Shorthand version.
     * @see Fn#field(Class, String) to specify the parent DTO class explicitly to avoid potential ambiguity.
     */
    public static ExpressionSpec field(final String field) {
        return f(field);
    }

    /**
     * Selects a DTO field by name for the specified DTO type that is selected/joined in the query.
     *
     * @param dtoClass The DTO class.
     * @param field    The name of the DTO field to select.
     * @return a query expression selecting the target entity/DTO field
     * @see Fn#f(Class, String) Shorthand version.
     * @see Fn#alias(Class, String, String) to specify a custom alias for the field.
     */
    public static ExpressionSpec field(final Class<?> dtoClass, final String field) {
        return f(dtoClass, field);
    }

    /**
     * Selects a DTO field by name for the specified DTO type that is selected/joined in the query, and aliases
     * it with the specified alias.
     *
     * @param dtoClass The DTO class to select from.
     * @param field    The name of the DTO field to select.
     * @param alias    The alias to use for the field.
     * @return a query expression selecting the aliased target entity/DTO field
     */
    public static ExpressionSpec alias(final Class<?> dtoClass, final String field, final String alias) {
        return new ProtoColumnExpressionSpec(SelectColumnSpec.class, field, alias, new Object[]{dtoClass});
    }

    /**
     * Selects a database column by name.
     * <p>
     * This is shorthand for {@link #column(String)}.
     * <p>
     * Column names can be qualified with a table name, e.g. "table.column".
     *
     * @param column The name of the column to select.
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec c(final String column) {
        return ca(column, null);
    }

    /**
     * Selects a database column by name.
     * <p>
     * Column names can be qualified with a table name, e.g. "table.column".
     *
     * @param column The name of the column to select.
     * @return a query expression selecting the target column.
     * @see Fn#c(String) Shorthand version.
     * @see Fn#c(String, String) Allows specifying the source table name and column name separately.
     */
    public static ExpressionSpec column(final String column) {
        return c(column);
    }

    /**
     * Selects a database column by its table and simple column name.
     * <p>
     * Shorthand for {@link #column(String, String)}.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a query expression selecting the target column.
     * @see Fn#c(String) Generic version that allows qualified column names.
     */
    public static ExpressionSpec c(final String table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by its table and simple column name.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a query expression selecting the target column.
     * @see Fn#c(String, String) Shorthand version.
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
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec c(final Table table, final String column) {
        return ca(table, column, null);
    }

    /**
     * Selects a database column by name.
     *
     * @param table  The table to select the column from.
     * @param column The name of the column to select.
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec column(final Table table, final String column) {
        return c(table, column);
    }

    /**
     * Selects a database column by name and aliases it with the specified alias.
     * <p>
     * Shorthand for {@link #alias(Table, String, String)}.
     *
     * @param table       The table to select the column from.
     * @param column      The simple name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec ca(final Table table, final String column, final @Nullable String columnAlias) {
        return new SelectColumnSpec(new Column(table, column), columnAlias, null);
    }

    /**
     * Selects a database column by name and aliases it with the specified alias.
     *
     * @param table       The table to select the column from.
     * @param column      The simple name of the column to select.
     * @param columnAlias The alias to use for the column; may be {@code null}.
     * @return a query expression selecting the target column.
     * @see #ca(Table, String, String) Shorthand version.
     * @see #alias(String, String) Generic version that allows qualified column names.
     */
    public static ExpressionSpec alias(final Table table, final String column, final String columnAlias) {
        return ca(table, column, columnAlias);
    }

    /**
     * Selects a database column by name and aliases it with the specified alias.
     * <p>
     * Shorthand for {@link #alias(String, String)}
     * <p>
     * Column names can be qualified with a table name, e.g. "table.column".
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec ca(final String column, final @Nullable String alias) {
        return new ProtoColumnExpressionSpec(SelectColumnSpec.class, column, alias);
    }

    /**
     * Selects a database column by name and aliases it with the specified alias.
     * <p>
     * Column names can be qualified with a table name, e.g. "table.column".
     *
     * @param column The name of the column to select.
     * @param alias  The alias to use for the column.
     * @return a query expression selecting the target column.
     * @see #ca(String, String) Shorthand version.
     */
    public static ExpressionSpec alias(final String column, final String alias) {
        return ca(column, alias);
    }

    /**
     * Selects a database column by name and alias.
     * <p>
     * Shorthand for {@link #alias(Table, String, String)}
     *
     * @param table       The table to select the column from.
     * @param column      The simple name of the column to select.
     * @param columnAlias The alias to use for the column.
     * @return a query expression selecting the target column.
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
     * @return a query expression selecting the target column.
     */
    public static ExpressionSpec alias(final String table, final String column, final @Nullable String columnAlias) {
        return ca(table, column, columnAlias);
    }

    // Alias references and literals

    /**
     * References an aliased column in the query.
     * <p>
     * This is used to provide aliases as part of conditional statements in various SQL clauses.
     *
     * @param alias The alias/label of the column in the query.
     * @return A reference to an aliased column in the query.
     */
    public static AliasReferenceSpec aliasRef(final String alias) {
        final List<String> aliasParts = StringUtils.split(alias, '.');

        if (aliasParts.size() > 1) {
            return aliasRef(aliasParts.get(0), aliasParts.get(1));
        } else {
            return new AliasReferenceSpec(alias);
        }
    }

    /**
     * References an aliased column in the query in the context of a parent table/subquery.
     * <p>
     * This is used to provide aliases as part of conditional statements in various SQL clauses.
     *
     * @param tableAlias  The alias/label of the parent table/subquery.
     * @param columnLabel The label (alias or column name) of the column to reference.
     * @return A reference to an aliased column in the query.
     * @see #aliasRef(String, ExpressionSpec) Expression-based alternative.
     */
    public static AliasReferenceSpec aliasRef(final String tableAlias, final String columnLabel) {
        return new AliasReferenceSpec(columnLabel, tableAlias);
    }

    /**
     * References an aliased column in the query in the context of a parent table/subquery.
     * <p>
     * This is used to provide aliases as part of conditional statements in various SQL clauses.
     *
     * @param tableAlias The alias/label of the parent table/subquery.
     * @param expression Query expression targeting a column in the parent table/subquery.
     * @return A reference to an aliased column in the query.
     * @see #aliasRef(String, ExpressionSpec) Simple string-based alternative.
     */
    public static AliasReferenceSpec aliasRef(final String tableAlias, final ExpressionSpec expression) {
        return new AliasReferenceSpec(expression, tableAlias);
    }

    /**
     * Specifies a literal value.
     * <p>
     * This is used to specify a literal value in a query expression.
     *
     * @param value The literal value to wrap.
     * @param <T>   The type of the literal value.
     * @return A literal expression specification.
     */
    public static <T> LiteralExpressionSpec<T> literal(final T value) {
        return new LiteralExpressionSpec<>(value);
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
     * {@code
     * litebridge.select(Fn.convert(Fn.avg(column), Long.class));
     * }
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
