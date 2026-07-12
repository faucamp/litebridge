package org.litebridge.db.spi.expression;

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
    SelectReference create(Column column);
}
