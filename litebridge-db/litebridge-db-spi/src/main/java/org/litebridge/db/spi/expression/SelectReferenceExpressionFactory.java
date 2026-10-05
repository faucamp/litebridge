package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

/**
 * Factory to create references to selected columns.
 */
@FunctionalInterface
public interface SelectReferenceExpressionFactory {

    /**
     * Creates a reference to a selected column.
     *
     * @param column The selected column to reference.
     * @return A new reference expression.
     */
    ColumnReference create(Column column, @Nullable String alias, @Nullable String tableAlias);
}
