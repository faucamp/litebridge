package org.litebridge.orm.expression.function.aggregate;

import org.litebridge.orm.expression.AbstractTODelegateExpressionSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;

/**
 * {@code AVG()}: Returns the average value of a column.
 *
 * @param <T> The return type of the expression result.
 */
public final class AvgSpec<T> extends AbstractTODelegateExpressionSpec<T> {

    /**
     * Creates a new {@code AvgSpec} instance.
     *
     * @param target     The target nested expression
     * @param returnType The return type of the expression result.
     */
    public AvgSpec(final ColumnExpressionSpec target, final Class<T> returnType) {
        super(target, returnType);
    }
}
