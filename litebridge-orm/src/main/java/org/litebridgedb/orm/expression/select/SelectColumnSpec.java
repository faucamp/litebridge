package org.litebridgedb.orm.expression.select;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

/**
 * Expression that selects a database lhs.
 *
 * @param column The lhs to select.
 */
public record SelectColumnSpec(Column column) implements ColumnExpressionSpec {
}
