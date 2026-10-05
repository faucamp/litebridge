package org.litebridge.db.oracle.api;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.oracle.api.insert.InsertAllStep;
import org.litebridge.db.oracle.engine.OracleInsertAllEngine;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.config.LitebridgeConfig;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

public class LitebridgeOracle extends Litebridge {

    private final OracleInsertAllEngine oracleInsertAllEngine;

    public LitebridgeOracle(final DatabaseProvider databaseProvider,
                            final TransactionManager transactionManager,
                            final @Nullable LitebridgeConfig litebridgeConfig,
                            final MethodHandles.Lookup lookup,
                            final OracleInsertAllEngine oracleInsertAllEngine) {
        super(databaseProvider, transactionManager, litebridgeConfig, lookup);
        this.oracleInsertAllEngine = oracleInsertAllEngine;
    }

    public InsertResult insertAll(final Function<InsertAllStep, InsertAllStep> insert) {
        return oracleInsertAllEngine.insertAll(insert, this::createLitebridgeContext);
    }
}
