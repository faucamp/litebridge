package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.delete.DeleteTerminal;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public final class SqlDeleteWhereConditionClauseTerminalImpl

        implements
        SqlDeleteWhereConditionClauseTerminal,
        DeleteTerminal {

    private final SqlDeletor delegate;

    public SqlDeleteWhereConditionClauseTerminalImpl(final SqlDeletor delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }

    @Override
    public SqlDeleteWhereConditionClause and(final String column) {
        return delegate.where(column);
    }

    @Override
    public SqlDeleteWhereConditionClause and(final ColumnExpressionSpec column) {
        return and(column.column().name());
    }
}
