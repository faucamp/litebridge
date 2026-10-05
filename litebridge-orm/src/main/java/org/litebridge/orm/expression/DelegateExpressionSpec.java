package org.litebridge.orm.expression;

import org.litebridge.db.spi.Column;

/**
 * A query expression that can nest another query expression.
 */
public sealed interface DelegateExpressionSpec extends ColumnExpressionSpec
        permits AbstractTODelegateExpressionSpec, StringTODelegateExpressionSpec {

    /**
     * Gets the target nested expression.
     *
     * @return the target nested expression.
     */
    ColumnExpressionSpec target();

    /**
     * Gets the target column of this expression.
     * <p>
     * The default implementation delegates to {@link #target()}.
     *
     * @return the target column.
     */
    @Override
    default Column getColumn() {
        return target().getColumn();
    }

    @Override
    default void setColumn(Column column) {
        target().setColumn(column);
    }
}
