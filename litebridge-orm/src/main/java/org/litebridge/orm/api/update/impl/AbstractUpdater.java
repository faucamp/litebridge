package org.litebridge.orm.api.update.impl;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.sql.update.SqlUpdater;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;
import java.util.Objects;

public abstract sealed class AbstractUpdater<US extends UpdateSpec> implements UpdateTerminal
        permits DtoUpdater, SqlUpdater {

    protected final US updateSpec;
    protected final TransactionalDatabaseProvider databaseProvider;

    protected AbstractUpdater(final US updateSpec,
                              final TransactionalDatabaseProvider databaseProvider) {
        this.updateSpec = updateSpec;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public UpdateResult execute() {
        return execute(updateSpec);
    }

    protected UpdateResult execute(final US updateSpec) {
        // Execute SQL query
        final UpdateResult updateResult;

        try {
            updateResult = databaseProvider.update(updateSpec.toUpdate(), databaseProvider.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute update", ex);
        }

        return updateResult;
    }

    public US updateSpec() {
        return updateSpec;
    }
}
