package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.ColumnExpression;

public class AliasedColumnExpression extends ColumnExpression {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public AliasedColumnExpression(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column);
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    @Override
    public String toSql(final Operation operation) {
        return id(column, operation);
    }

    public String toSqlWithAlias(final Operation operation) {
        return idWithAlias(column, operation);
    }

    protected String id(final Column column, final Operation operation) {
        return columnIdentifierGenerator.createColumnIdentifier(column, false, operation);
    }

    protected String idWithAlias(final Column column, final Operation operation) {
        return columnIdentifierGenerator.createColumnIdentifier(column, true, operation);
    }
}
