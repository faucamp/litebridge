package org.litebridgedb.orm.api.update.impl;

import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.api.dto.update.DtoUpdater;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.sql.update.SqlUpdater;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.api.update.model.UpdateSpec;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;

public abstract sealed class AbstractUpdater<US extends UpdateSpec> implements UpdateTerminal
        permits DtoUpdater, SqlUpdater {

    protected final US updateSpec;
    protected final TransactionalDatabaseProvider databaseProvider;
    protected final LitebridgeContext litebridgeContext;

    protected AbstractUpdater(final US updateSpec,
                              final TransactionalDatabaseProvider databaseProvider,
                              final LitebridgeContext litebridgeContext) {
        this.updateSpec = updateSpec;
        this.databaseProvider = databaseProvider;
        this.litebridgeContext = litebridgeContext;
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
