package org.litebridge.orm.expression.function.aggregate;

import org.litebridge.orm.expression.AbstractTODelegateExpressionSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;

/**
 * {@code MAX()}: Returns the highest or largest value within a specified column or expression
 *
 * @param <T> The return type of the expression result.
 */
public final class MaxSpec<T> extends AbstractTODelegateExpressionSpec<T> {

    /**
     * Creates a new {@code MaxSpec} instance.
     *
     * @param target     The target nested expression
     * @param returnType The return type of the expression result.
     */
    public MaxSpec(final ColumnExpressionSpec target, final Class<T> returnType) {
        super(target, returnType);
    }
}
