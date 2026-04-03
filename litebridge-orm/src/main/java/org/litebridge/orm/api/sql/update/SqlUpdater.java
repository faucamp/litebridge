package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.update.impl.AbstractUpdater;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

public final class SqlUpdater extends AbstractUpdater<UpdateSpec> implements SqlUpdateStep {

    public SqlUpdater(final TableMetaData tableMetaData, final TransactionalDatabaseProvider databaseProvider) {
        super(new UpdateSpec(), databaseProvider);
        updateSpec.setTableMetaData(tableMetaData);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return new SqlUpdateWhereConditionClause(updateSpec.newWhereCondition(new Column(updateSpec.getTableMetaData().toTable(), column)), new SqlUpdateWhereConditionClauseTerminalImpl(this));
    }

    @Override
    public SqlUpdateSetStep set(final String column) {
        return new SqlUpdateSetStep(updateSpec.getTableMetaData().column(column).toColumn(), this);
    }
}
