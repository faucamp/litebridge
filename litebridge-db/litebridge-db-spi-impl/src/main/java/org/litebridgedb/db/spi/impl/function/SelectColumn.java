package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;

public class SelectColumn extends AliasedColumnExpression {

    public SelectColumn(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column, columnIdentifierGenerator);
    }

    @Override
    public String toSql(final Operation operation) {
        return column.name();
    }
}
