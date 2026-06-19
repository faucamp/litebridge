package org.litebridgedb.orm.expression.select;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpression;

/**
 * Expression that selects a database column.
 *
 * @param column The column to select.
 */
public record SelectColumn(Column column) implements ColumnExpression {
}
