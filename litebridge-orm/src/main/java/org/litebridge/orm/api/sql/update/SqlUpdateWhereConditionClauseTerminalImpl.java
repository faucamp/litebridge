package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;

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
}
