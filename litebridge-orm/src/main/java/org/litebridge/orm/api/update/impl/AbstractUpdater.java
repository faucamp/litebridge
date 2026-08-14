package org.litebridge.orm.api.update.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.sql.update.SqlUpdater;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.api.update.model.UpdateSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract sealed class AbstractUpdater<US extends UpdateSpec> implements UpdateTerminal
        permits DtoUpdater, SqlUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUpdater.class);

    protected final US updateSpec;
    protected final DatabaseProvider databaseProvider;
    protected final LitebridgeContext litebridgeContext;
    protected QueryNode node;

    protected AbstractUpdater(final US updateSpec,
                              final QueryNode node,
                              final LitebridgeContext litebridgeContext) {
        this.updateSpec = updateSpec;
        this.databaseProvider = litebridgeContext.databaseProvider();
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
        litebridgeContext.createQueryCompiler().compile(node, updateSpec);
        final PreparedOperation preparedOperation = updateSpec.toUpdate(litebridgeContext.tableMetaDataCache(), databaseProvider.getTypeConverter());
        final Update update = (Update) preparedOperation.operation();
        final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(update.table());
        // Generate SQL and create type conversion metadata
        final String sql = databaseProvider.toSql(update, litebridgeContext.transactionManager());
        final UpdateMetaData updateMetaData = createUpdateMetaData(tableMetaData);
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, updateMetaData, null));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaData);
        return execute(executionSql);
    }

    private UpdateResult execute(final PreparedSql preparedSql) {
        final UpdateResult updateResult;

        try {
            updateResult = databaseProvider.update(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute update", ex);
        }

        LOGGER.debug("Update result: {}", updateResult);
        return updateResult;
    }

    public void addSetNode(final Column column, final Object value) {
        this.node = new SetNode(this.node, column, value);
    }

    public US updateSpec() {
        return updateSpec;
    }

    public QueryNode node() {
        return node;
    }

    private UpdateMetaData createUpdateMetaData(final TableMetaData tableMetaData) {
        final List<ColumnMetaData> generatedPrimaryKeyColumns = getGeneratedPrimaryKeyColumns(tableMetaData);

        if (generatedPrimaryKeyColumns.isEmpty()) {
            return new UpdateMetaData(false, Collections.emptyList(), new String[0]);
        }

        final String[] generatedPkColumnNames = generatedPrimaryKeyColumns.stream()
                .map(ColumnMetaData::name)
                .toArray(String[]::new);

        return new UpdateMetaData(true, generatedPrimaryKeyColumns, generatedPkColumnNames);
    }

    /**
     * Get the primary key columns for which the database generates values.
     *
     * @param tableMetaData the {@link TableMetaData} object containing the metadata of the target table
     * @return a list of {@link ColumnMetaData} objects representing the generated primary key columns
     */
    private List<ColumnMetaData> getGeneratedPrimaryKeyColumns(final TableMetaData tableMetaData) {
        return tableMetaData.primaryKey().stream()
                .filter(columnMetadata -> columnMetadata.isAutoIncrement()
                        || (columnMetadata.getGenerator() != null && SequenceColumnValueGenerator.class.isAssignableFrom(columnMetadata.getGenerator().getClass())))
                .toList();
    }
}
