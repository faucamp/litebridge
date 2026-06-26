package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Column;

/**
 * Factory to create references to selected columns.
 */
@FunctionalInterface
public interface SelectReferenceExpressionFactory {

    /**
     * Creates a reference to a selected lhs.
     *
     * @param column The selected lhs to reference.
     * @return A new reference expression.
     */
    SelectReference create(Column column);
}
