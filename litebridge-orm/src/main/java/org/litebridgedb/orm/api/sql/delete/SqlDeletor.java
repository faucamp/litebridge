package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public final class SqlDeletor extends AbstractDeletor<DeleteSpec> implements SqlDeleteWhereClause {

    private final LitebridgeContext litebridgeContext;

    public SqlDeletor(final Table table,
                      final TransactionalDatabaseProvider databaseProvider,
                      final LitebridgeContext litebridgeContext) {
        super(new DeleteSpec(litebridgeContext.selectExpressionMapper()), databaseProvider);
        deleteSpec.setTable(table);
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlDeleteWhereConditionClause where(final String column) {
        return new SqlDeleteWhereConditionClause(deleteSpec.newWhereCondition(new Column(deleteSpec.getTable(), column)), new SqlDeleteWhereConditionClauseTerminalImpl(this), litebridgeContext);
    }
}
