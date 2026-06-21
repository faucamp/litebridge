package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

public class SqlDeleteFromClause {

    private final TransactionalDatabaseProvider databaseProvider;
    private final LitebridgeContext litebridgeContext;

    public SqlDeleteFromClause(final TransactionalDatabaseProvider databaseProvider, final LitebridgeContext litebridgeContext) {
        this.databaseProvider = databaseProvider;
        this.litebridgeContext = litebridgeContext;
    }

    public SqlDeletor from(final String tableName) {
        return new SqlDeletor(new Table(tableName, null), databaseProvider, litebridgeContext);
    }
}
