package org.litebridgedb.db.spi.function;

import org.litebridgedb.db.spi.query.ColumnExpressionFactory;
import org.litebridgedb.db.spi.query.SelectExpression;

/**
 * SQL function registry.
 *
 * @param selectColumnFactory Factory to create column expression to specify a column to be selected.
 * @param aggregate           Aggregate functions perform calculations on a set of values and return a single value.
 * @param scalar              Scalar functions perform calculations on one or more input values and return a single value.
 */
public record SqlFunctionRegistry(
        ColumnExpressionFactory selectColumnFactory,
        Aggregate aggregate,
        Scalar scalar) {

    /**
     * Aggregate functions perform calculations on a set of values and return a single value.
     *
     * @param avg   AVG(): Average value of the specified column.
     * @param count COUNT(): Total number of rows in the query result.
     */
    public record Aggregate(
            ColumnExpressionFactory avg,
            SelectExpression count) {
    }

    /**
     * Scalar functions perform calculations on one or more input values and return a single value.
     *
     * @param ucase UCASE(): Convert a string to uppercase.
     */
    public record Scalar(
            ColumnExpressionFactory ucase) {
    }
}
