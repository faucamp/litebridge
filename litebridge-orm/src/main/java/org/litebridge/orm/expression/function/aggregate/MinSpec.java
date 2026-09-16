package org.litebridge.orm.expression.function.aggregate;

import org.litebridge.orm.expression.AbstractTODelegateExpressionSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;

/**
 * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
 *
 * @param <T> The return type of the expression result.
 */
public final class MinSpec<T> extends AbstractTODelegateExpressionSpec<T> {

    /**
     * Creates a new {@code MinSpec} instance.
     *
     * @param target     The target nested expression
     * @param returnType The return type of the expression result.
     *
     */
    public MinSpec(final ColumnExpressionSpec target, final Class<T> returnType) {
        super(target, returnType);
    }
}
