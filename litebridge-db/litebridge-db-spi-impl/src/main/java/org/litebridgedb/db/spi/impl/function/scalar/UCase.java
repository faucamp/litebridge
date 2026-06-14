package org.litebridgedb.db.spi.impl.function.scalar;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.AliasedColumnExpression;

public class UCase extends AliasedColumnExpression {

    public UCase(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column, columnIdentifierGenerator);
    }

    @Override
    public String toSql() {
        return "UCASE(%s)".formatted(id(column));
    }

    @Override
    public String toSqlWithAlias() {
        return "UCASE(%s)".formatted(idWithAlias(column));
    }
}
