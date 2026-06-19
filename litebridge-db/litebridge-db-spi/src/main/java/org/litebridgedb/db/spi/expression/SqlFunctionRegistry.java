package org.litebridgedb.db.spi.expression;

/**
 * SQL function registry.
 *
 * @param selectColumnFactory Factory to create column expression to specify a column to be selected.
 * @param aggregate           Aggregate functions perform calculations on a set of values and return a single value.
 * @param scalar              Scalar functions perform calculations on one or more input values and return a single value.
 * @param date                Date/time functions.
 */
public record SqlFunctionRegistry(
        ColumnExpressionFactory selectColumnFactory,
        Aggregate aggregate,
        Scalar scalar,
        Date date) {

    /**
     * Aggregate functions perform calculations on a set of values and return a single value.
     *
     * @param avg   AVG(): Average value of the specified column.
     * @param min   MIN(): Minimum value of the specified column.
     * @param max   MAX(): Maximum value of the specified column.
     * @param count COUNT(): Total number of rows in the query result.
     */
    public record Aggregate(
            NestableExpressionFactory avg,
            NestableExpressionFactory min,
            NestableExpressionFactory max,
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
            NestableExpressionFactory upper,
            NestableExpressionFactory lower,
            NestableExpressionFactory substring,
            NestableExpressionFactory abs) {
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
