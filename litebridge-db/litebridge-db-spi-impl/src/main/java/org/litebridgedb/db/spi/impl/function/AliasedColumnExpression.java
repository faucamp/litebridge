package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.ColumnExpression;

public class AliasedColumnExpression extends ColumnExpression {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public AliasedColumnExpression(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column);
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    @Override
    public String toSql() {
        return id(column);
    }

    public String toSqlWithAlias() {
        return idWithAlias(column);
    }

    protected String id(final Column column) {
        return columnIdentifierGenerator.createColumnIdentifier(column, false);
    }

    protected String idWithAlias(final Column column) {
        return columnIdentifierGenerator.createColumnIdentifier(column, true);
    }
}
