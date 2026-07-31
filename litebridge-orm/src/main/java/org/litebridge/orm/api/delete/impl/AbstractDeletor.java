package org.litebridge.orm.api.delete.impl;

import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.ast.QueryNode;

import java.sql.SQLException;

public abstract sealed class AbstractDeletor<DS extends DeleteSpec> implements DeleteTerminal
permits DtoDeletor, SqlDeletor {

    protected final DS deleteSpec;
    protected final TransactionalDatabaseProvider databaseProvider;
    protected final LitebridgeContext litebridgeContext;
    protected QueryNode node;

    protected AbstractDeletor(final DS deleteSpec,
                              final TransactionalDatabaseProvider databaseProvider,
                              final LitebridgeContext litebridgeContext,
                              final QueryNode node) {
        this.deleteSpec = deleteSpec;
        this.databaseProvider = databaseProvider;
        this.litebridgeContext = litebridgeContext;
        this.node = node;
    }

    @Override
    public UpdateResult execute() {
        litebridgeContext.createQueryCompiler().compile(node, deleteSpec);
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
