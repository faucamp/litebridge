package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

/**
 * Query expression encapsulating a target column.
 */
public sealed interface ColumnExpressionSpec
        extends Aliasable, ExpressionSpec
        permits AbstractColumnExpressionSpec, DelegateExpressionSpec {

    /**
     * Gets the target column of this expression.
     *
     * @return the target column.
     */
    Column getColumn();

    /**
     * Sets the target column for this expression.
     *
     * @param column the target column to set.
     */
    void setColumn(Column column);

    @Nullable String getTableAlias();

    void setTableAlias(@Nullable String tableAlias);
}
