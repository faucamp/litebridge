package org.litebridge.db.spi.impl.sql;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.Update;

public class DefaultSqlGenerator implements SqlGenerator {

    protected final MetaDataEngine metaDataEngine;
    protected final ColumnIdentifierGenerator columnIdentifierGenerator;
    protected final MathOperationGenerator mathOperationGenerator;

    protected final ConcurrentLazy<SelectSqlGenerator> selectSqlGenerator = new ConcurrentLazy<>(this::createSelectSqlGenerator);
    protected final ConcurrentLazy<InsertSqlGenerator> insertSqlGenerator = new ConcurrentLazy<>(this::createInsertSqlGenerator);
    protected final ConcurrentLazy<UpdateSqlGenerator> updateSqlGenerator = new ConcurrentLazy<>(this::createUpdateSqlGenerator);
    protected final ConcurrentLazy<DeleteSqlGenerator> deleteSqlGenerator = new ConcurrentLazy<>(this::createDeleteSqlGenerator);
    protected final ConcurrentLazy<MergeSqlGenerator> mergeSqlGenerator = new ConcurrentLazy<>(this::createMergeSqlGenerator);

    public DefaultSqlGenerator(final MetaDataEngine metaDataEngine,
                               final ColumnIdentifierGenerator columnIdentifierGenerator,
                               final MathOperationGenerator mathOperationGenerator) {
        this.metaDataEngine = metaDataEngine;
        this.columnIdentifierGenerator = columnIdentifierGenerator;
        this.mathOperationGenerator = mathOperationGenerator;
    }

    @Override
    public String generateSql(final Operation operation, final ConnectionProvider connectionProvider) {
        return switch (operation) {
            case Select select -> selectSqlGenerator.getOrThrow().generateSql(select, connectionProvider);
            case Insert insert -> insertSqlGenerator.getOrThrow().generateSql(insert, connectionProvider);
            case Update update -> updateSqlGenerator.getOrThrow().generateSql(update, connectionProvider);
            case Delete delete -> deleteSqlGenerator.getOrThrow().generateSql(delete, connectionProvider);
            case Merge merge -> mergeSqlGenerator.getOrThrow().generateSql(merge, connectionProvider);
        };
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
        return new SelectSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create an {@link InsertSqlGenerator} instance for the database provider.
     *
     * @return an {@link InsertSqlGenerator} instance
     */
    protected InsertSqlGenerator createInsertSqlGenerator() {
        return new InsertSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData,
                metaDataEngine.metaData().insertCapability());
    }

    /**
     * Create an {@link UpdateSqlGenerator} instance for the database provider.
     *
     * @return an {@link UpdateSqlGenerator} instance
     */
    protected UpdateSqlGenerator createUpdateSqlGenerator() {
        return new UpdateSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create a {@link DeleteSqlGenerator} instance for the database provider.
     *
     * @return a {@link DeleteSqlGenerator} instance
     */
    protected DeleteSqlGenerator createDeleteSqlGenerator() {
        return new DeleteSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }

    /**
     * Create a {@link MergeSqlGenerator} instance for the database provider.
     *
     * @return a {@link MergeSqlGenerator} instance
     */
    protected MergeSqlGenerator createMergeSqlGenerator() {
        return new MergeSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }
}
