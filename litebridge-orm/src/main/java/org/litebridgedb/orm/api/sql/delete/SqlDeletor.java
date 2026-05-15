package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.delete.impl.AbstractDeletor;
import org.litebridgedb.orm.api.delete.model.DeleteSpec;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

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
