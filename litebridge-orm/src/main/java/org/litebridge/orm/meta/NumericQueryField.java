package org.litebridge.orm.meta;

import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.expression.ProtoNestableTOExpr;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

/**
 * Metamodel field for querying numeric-based columns in a type-safe manner.
 * <p>
 * This class extends the capabilities of {@link QueryField}
 * by providing methods for common numeric operations, such as retrieving average, maximum,
 * or minimum values.
 */
public final class NumericQueryField extends QueryField {

    public NumericQueryField(final Class<?> dtoClass, final String fieldName) {
        super(dtoClass, fieldName);
    }

    /**
     * {@code AVG()}: Returns the average value of a column/field.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select the average value of a column/field.
     */
    public TypeOverrideExpressionSpec<Number> avg() {
        return Fn.avg(Fn.f(dtoClass, field));
    }

    /**
     * {@code MAX()}: Returns the highest or largest value within a specified field.
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a field.
     */
    public TypeOverrideExpressionSpec<Number> max() {
        return Fn.max(Fn.f(dtoClass, field));
    }

    /**
     * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
     *
     * @return a {@link ProtoNestableTOExpr} expression instance to select the maximum value of a column/field.
     */
    public TypeOverrideExpressionSpec<Number> min() {
        return Fn.min(Fn.f(dtoClass, field));
    }
}
