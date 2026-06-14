package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;

@FunctionalInterface
public interface ColumnExpressionFactory {

    ColumnExpression create(Column column);
}
