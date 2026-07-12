package org.litebridge.db.spi.expression;

/**
 * SQL function registry.
 *
 * @param select    Expressions dealing with selecting columns, sub-selects, literals, and references to selected columns.
 * @param aggregate Aggregate functions perform calculations on a set of values and return a single value.
 * @param scalar    Scalar functions perform calculations on one or more input values and return a single value.
 * @param date      Date/time functions.
 */
public record SqlFunctionRegistry(
        Select select,
        Aggregate aggregate,
        Scalar scalar,
        Date date) {


    /**
     * Expressions dealing with selecting columns, sub-selects, literals, and references to selected columns.
     *
     * @param column    Factory to create column expression to specify a column to be selected.
     * @param subselect Factory to create sub-select expressions.
     * @param literal   Factory to create literal expressions.
     * @param reference Factory to create selected column reference expressions.
     */
    public record Select(
            ColumnExpressionFactory column,
            SubselectExpressionFactory subselect,
            LiteralExpressionFactory literal,
            SelectReferenceExpressionFactory reference) {
    }

    /**
     * Aggregate functions perform calculations on a set of values and return a single value.
     *
     * @param avg   AVG(): Average value of the specified column.
     * @param min   MIN(): Minimum value of the specified column.
     * @param max   MAX(): Maximum value of the specified column.
     * @param count COUNT(): Total number of rows in the query result.
     */
    public record Aggregate(
            DelegateExpressionFactory avg,
            DelegateExpressionFactory min,
            DelegateExpressionFactory max,
            SelectExpression count) {
    }

    /**
     * Scalar functions perform calculations on one or more input values and return a single value.
     *
     * @param lower     LOWER(): Convert a string to lowercase.
     * @param upper     UPPER(): Convert a string to uppercase.
     * @param substring SUBSTRING(): Extract a substring from a string.
     * @param abs       ABS(): Absolute value of a number.
     */
    public record Scalar(
            DelegateExpressionFactory upper,
            DelegateExpressionFactory lower,
            DelegateExpressionFactory substring,
            DelegateExpressionFactory abs) {
    }

    /**
     * Date functions.
     *
     * @param currentTimestamp CURRENT_TIMESTAMP(): Current date/time.
     */
    public record Date(
            SelectExpression currentTimestamp
    ) {
    }
}
