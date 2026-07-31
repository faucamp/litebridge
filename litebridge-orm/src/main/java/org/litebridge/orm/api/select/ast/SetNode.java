package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

/**
 * Represents a SET clause in an UPDATE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param column   the column to update
 * @param value    the value to set (can be a raw value or a {@link org.litebridge.db.spi.math.MathOperation})
 */
public record SetNode(@Nullable QueryNode previous, Column column, Object value) implements QueryNode {
}
