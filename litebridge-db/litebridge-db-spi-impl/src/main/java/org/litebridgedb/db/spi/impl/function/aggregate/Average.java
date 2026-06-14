package org.litebridgedb.db.spi.impl.function.aggregate;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.AliasedColumnExpression;

public class Average extends AliasedColumnExpression {

    public Average(final Column column, ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column, columnIdentifierGenerator);
    }

    @Override
    public String toSqlWithAlias() {
        return "AVG(%s)".formatted(idWithAlias(column));
    }

    @Override
    public String toSql() {
        return "AVG(%s)".formatted(id(column));
    }
}
