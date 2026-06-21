package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.api.spec.FieldColumnSpec;
import org.litebridgedb.orm.api.update.impl.AbstractUpdater;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class SqlUpdater extends AbstractUpdater<UpdateSpec> implements SqlUpdateStep {

    public SqlUpdater(final Table table,
                      final TransactionalDatabaseProvider databaseProvider,
                      final LitebridgeContext litebridgeContext) {
        super(new UpdateSpec(litebridgeContext.selectExpressionMapper()), databaseProvider, litebridgeContext);
        updateSpec.setTable(table);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return new SqlUpdateWhereConditionClause(updateSpec.newWhereCondition(new Column(updateSpec.getTable(), column)), new SqlUpdateWhereConditionClauseTerminalImpl(this), litebridgeContext);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final FieldColumnSpec column) {
        return where(column.columnSpec().name());
    }

    @Override
    public SqlUpdateSetStep set(final String column) {
        final Column col = new Column(updateSpec.getTable(), column);
        return new SqlUpdateSetStep(col, this);
    }

    @Override
    public SqlUpdateSetStep set(final FieldColumnSpec column) {
        return set(column.columnSpec().name());
    }
}
