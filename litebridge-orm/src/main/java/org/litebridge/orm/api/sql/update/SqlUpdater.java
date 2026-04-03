package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

public final class SqlUpdater extends AbstractUpdater<UpdateSpec> implements SqlUpdateStep {

    public SqlUpdater(final Table table, final TransactionalDatabaseProvider databaseProvider) {
        super(new UpdateSpec(), databaseProvider);
        updateSpec.setTable(table);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return new SqlUpdateWhereConditionClause(updateSpec.newWhereCondition(new Column(updateSpec.getTable(), column)), new SqlUpdateWhereConditionClauseTerminalImpl(this));
    }

    @Override
    public SqlUpdateSetStep set(final String column) {
        final Column col = new Column(updateSpec.getTable(), column);
        return new SqlUpdateSetStep(col, this);
    }
}
