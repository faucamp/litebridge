package org.litebridge.orm.api.delete.impl;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;

public abstract sealed class AbstractDeletor<DS extends DeleteSpec> implements DeleteTerminal
permits DtoDeletor, SqlDeletor {

    protected final DS deleteSpec;
    protected final TransactionalDatabaseProvider databaseProvider;

    protected AbstractDeletor(final DS deleteSpec,
                              final TransactionalDatabaseProvider databaseProvider) {
        this.deleteSpec = deleteSpec;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public UpdateResult execute() {
        return execute(deleteSpec);
    }

    protected UpdateResult execute(final DS deleteSpec) {
        // Execute SQL query
        final UpdateResult updateResult;

        try {
            updateResult = databaseProvider.delete(deleteSpec.toDelete(), databaseProvider.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return updateResult;
    }

    protected DS deleteSpec() {
        return deleteSpec;
    }
}
