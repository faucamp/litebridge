package org.litebridge.orm;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.SqlMergeUsingStep;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.engine.MergeEngine;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;

public class Litebridge extends LitebridgeCore {

    private final MergeEngine mergeEngine = new MergeEngine();

    public Litebridge(final DatabaseProvider databaseProvider, final DataSource dataSource) {
        super(databaseProvider, dataSource);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final DataSource dataSource, final @Nullable LitebridgeConfig litebridgeConfig) {
        super(databaseProvider, dataSource, litebridgeConfig);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final DataSource dataSource, final @Nullable LitebridgeConfig litebridgeConfig, final MethodHandles.Lookup lookup) {
        super(databaseProvider, dataSource, litebridgeConfig, lookup);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final TransactionManager transactionManager) {
        super(databaseProvider, transactionManager);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final TransactionManager transactionManager, final LitebridgeConfig litebridgeConfig) {
        super(databaseProvider, transactionManager, litebridgeConfig);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final TransactionManager transactionManager, final MethodHandles.Lookup lookup) {
        super(databaseProvider, transactionManager, lookup);
    }

    public Litebridge(final DatabaseProvider databaseProvider, final TransactionManager transactionManager, final @Nullable LitebridgeConfig litebridgeConfig, final MethodHandles.Lookup lookup) {
        super(databaseProvider, transactionManager, litebridgeConfig, lookup);
    }

    /**
     * Performs a {@code MERGE INTO} operation targeting the specified database table.
     *
     * @param tableName the name of the table into which to merge data
     * @param merge     a function that takes an instance of {@link SqlMergeUsingStep} and returns a {@code MergeTerminal},
     *                  specifying the conditions and actions for merging records
     * @return the result of the merge operation
     */
    public UpdateResult mergeInto(final String tableName, final Function<SqlMergeUsingStep, MergeTerminal> merge) {
        return mergeEngine.mergeInto(tableName, merge, createSqlLitebridgeContext());
    }

    /**
     * Performs a {@code MERGE INTO} operation targeting the specified mapped DTO type.
     *
     * @param dtoClass the class of the DTO/entity to merge
     * @param merge    a function that takes an instance of {@link DtoMergeUsingStep} and returns a {@code MergeTerminal},
     *                 specifying the conditions and actions for merging records
     * @param <DTO>    the type of the DTO
     * @return the result of the merge operation
     */
    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass, final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge) {
        return mergeEngine.mergeInto(dtoClass, merge, createDtoLitebridgeContext());
    }
}
