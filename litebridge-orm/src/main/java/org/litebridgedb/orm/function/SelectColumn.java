package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

/**
 * Expression that selects a database column.
 *
 * @param column The column to select.
 */
public record SelectColumn(Column column) implements ColumnExpression {
}
