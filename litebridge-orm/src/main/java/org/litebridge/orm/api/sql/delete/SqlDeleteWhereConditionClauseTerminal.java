package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteWhereConditionClauseTerminal;

public class SqlDeleteWhereConditionClauseTerminal

        implements DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal> {

    private final SqlDeletor delegate;

    public SqlDeleteWhereConditionClauseTerminal(final SqlDeletor delegate) {
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
}
