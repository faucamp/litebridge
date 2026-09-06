package org.litebridge.db.spi.impl.sql;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.engine.DefaultMetaDataEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;

public class DefaultSqlGenerator implements SqlGenerator {

    protected final MetaDataEngine metaDataEngine;

    protected final ConcurrentLazy<ColumnIdentifierGenerator> columnIdentifierGenerator = new ConcurrentLazy<>(this::createColumnIdentifierGenerator);
    protected final ConcurrentLazy<SelectSqlGenerator> selectSqlGenerator = new ConcurrentLazy<>(this::createSelectSqlGenerator);
    protected final ConcurrentLazy<InsertSqlGenerator> insertSqlGenerator = new ConcurrentLazy<>(this::createInsertSqlGenerator);
    protected final ConcurrentLazy<UpdateSqlGenerator> updateSqlGenerator = new ConcurrentLazy<>(this::createUpdateSqlGenerator);
    protected final ConcurrentLazy<DeleteSqlGenerator> deleteSqlGenerator = new ConcurrentLazy<>(this::createDeleteSqlGenerator);
    protected final ConcurrentLazy<MergeSqlGenerator> mergeSqlGenerator = new ConcurrentLazy<>(this::createMergeSqlGenerator);

    public DefaultSqlGenerator(final MetaDataEngine metaDataEngine) {
        this.metaDataEngine = metaDataEngine;
    }

    public DefaultSqlGenerator() {
        this.metaDataEngine = new DefaultMetaDataEngine();
    }

    @Override
    public String generateSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return switch (operation) {
            case Select select -> selectSqlGenerator.getOrThrow().prepareSql(select, connectionProvider);
            case Insert insert -> insertSqlGenerator.getOrThrow().prepareSql(insert, connectionProvider);
            case Update update -> updateSqlGenerator.getOrThrow().prepareSql(update, connectionProvider);
            case Delete delete -> deleteSqlGenerator.getOrThrow().prepareSql(delete, connectionProvider);
            case Merge merge -> mergeSqlGenerator.getOrThrow().prepareSql(merge, connectionProvider);
        };
    }

    @Override
    public MetaDataEngine metaDataEngine() {
        return metaDataEngine;
    }

    @Override
    public SelectSqlGenerator selectSqlGenerator() {
        return selectSqlGenerator.getOrThrow();
    }

    /**
     * Create a {@link SelectSqlGenerator} instance for the database provider.
     *
     * @return a {@link SelectSqlGenerator} instance
     */
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new SelectSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create an {@link InsertSqlGenerator} instance for the database provider.
     *
     * @return an {@link InsertSqlGenerator} instance
     */
    protected InsertSqlGenerator createInsertSqlGenerator() {
        return new InsertSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create an {@link UpdateSqlGenerator} instance for the database provider.
     *
     * @return an {@link UpdateSqlGenerator} instance
     */
    protected UpdateSqlGenerator createUpdateSqlGenerator() {
        return new UpdateSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create a {@link DeleteSqlGenerator} instance for the database provider.
     *
     * @return a {@link DeleteSqlGenerator} instance
     */
    protected DeleteSqlGenerator createDeleteSqlGenerator() {
        return new DeleteSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create a {@link MergeSqlGenerator} instance for the database provider.
     *
     * @return a {@link MergeSqlGenerator} instance
     */
    protected MergeSqlGenerator createMergeSqlGenerator() {
        return new MergeSqlGenerator(
                columnIdentifierGenerator.getOrThrow(),
                metaDataEngine::ensureTableMetaData,
                insertSqlGenerator.getOrThrow(),
                updateSqlGenerator.getOrThrow(),
                deleteSqlGenerator.getOrThrow());
    }

    /**
     * Create a {@link ColumnIdentifierGenerator} instance for the database provider.
     *
     * @return a {@link ColumnIdentifierGenerator} instance
     */
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new ColumnIdentifierGenerator();
    }
}
