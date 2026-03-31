package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

public class SqlDeleteFromClause {

    private final TransactionalDatabaseProvider databaseProvider;

    public SqlDeleteFromClause(final TransactionalDatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public SqlDeletor from(final String tableName) {
        return new SqlDeletor(new Table(tableName, null), databaseProvider);
    }
}
