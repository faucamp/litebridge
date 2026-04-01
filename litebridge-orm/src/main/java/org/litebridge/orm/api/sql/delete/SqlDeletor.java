package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

public final class SqlDeletor extends AbstractDeletor<DeleteSpec> implements SqlDeleteWhereClause {

    public SqlDeletor(final Table table, final TransactionalDatabaseProvider databaseProvider) {
        super(new DeleteSpec(), databaseProvider);
        deleteSpec.setTable(table);
    }

    @Override
    public SqlDeleteWhereConditionClause where(final String column) {
        return new SqlDeleteWhereConditionClause(deleteSpec.newWhereCondition(new Column(deleteSpec.getTable(), column)), new SqlDeleteWhereConditionClauseTerminalImpl(this));
    }
}
