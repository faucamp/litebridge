package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public class SqlDeleteFromClause {

    private final TransactionalDatabaseProvider databaseProvider;

    public SqlDeleteFromClause(final TransactionalDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public SqlDeletor from(final String tableName) {
        return new SqlDeletor(new Table(tableName, null), databaseProvider);
    }
}
