package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

public final class SqlUpdateWhereConditionClauseTerminalImpl

        implements
        SqlUpdateWhereConditionClauseTerminal,
        UpdateTerminal {

    private final SqlUpdater delegate;

    public SqlUpdateWhereConditionClauseTerminalImpl(final SqlUpdater delegate) {
        this.delegate = delegate;
    }

    @Override
    public UpdateSpec updateSpec() {
        return delegate.updateSpec();
    }

    @Override
    public UpdateResult execute() {
        return delegate.execute();
    }

    @Override
    public SqlUpdateWhereConditionClause and(final String column) {
        return delegate.where(column);
    }

    @Override
    public SqlUpdateWhereConditionClause and(final ColumnExpressionSpec column) {
        return and(column.column().name());
    }
}
