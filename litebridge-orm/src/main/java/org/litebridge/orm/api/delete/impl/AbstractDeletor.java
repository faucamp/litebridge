package org.litebridge.orm.api.delete.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract sealed class AbstractDeletor<DS extends DeleteSpec> implements DeleteTerminal
        permits DtoDeletor, SqlDeletor {

    private static final UpdateMetaData UPDATE_META_DATA = new UpdateMetaData(false, Collections.emptyList(), new String[0]);

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
        final int nodeHash = Objects.requireNonNull(node).hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            // Extract bind values and executed cached query
            final List<@Nullable Object> rawBindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(rawBindValues));
        } else {
            // Compile and execute query (it will be cached as part of this process)
            return compileAndExecute(nodeHash);
        }
    }

    private UpdateResult compileAndExecute(final int astCacheKey) {
        // Compile/prepare SQL query
        litebridgeContext.createQueryCompiler().compile(node, deleteSpec);
        final PreparedOperation preparedOperation = deleteSpec.toDelete(litebridgeContext.tableMetaDataCache(), databaseProvider.getTypeConverter());
        final Delete delete = (Delete) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = databaseProvider.toSql(delete, databaseProvider.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, UPDATE_META_DATA, null));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, UPDATE_META_DATA);
        return execute(executionSql);
    }

    private UpdateResult execute(final PreparedSql preparedSql) {
        final UpdateResult updateResult;

        try {
            updateResult = databaseProvider.delete(preparedSql, databaseProvider.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute delete", ex);
        }

        return updateResult;
    }

    protected DS deleteSpec() {
        return deleteSpec;
    }
}
