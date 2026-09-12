package org.litebridge.db.oracle.api;

import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.api.insert.InsertAllStep;
import org.litebridge.db.oracle.engine.OracleInsertAllEngine;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.orm.engine.LitebridgeContext;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LitebridgeOracleTest {

    @Test
    void insertAll_delegatesToOracleInsertAllEngine() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        when(databaseProvider.aliasTransformer()).thenReturn(new UppercaseAliasTransformer());
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final OracleInsertAllEngine insertAllEngine = mock(OracleInsertAllEngine.class);

        final LitebridgeOracle litebridgeOracle = new LitebridgeOracle(
                databaseProvider,
                transactionManager,
                null,
                lookup,
                insertAllEngine);

        final InsertResult expectedResult = mock(InsertResult.class);
        when(insertAllEngine.insertAll(any(), any())).thenAnswer(invocation -> {
            final Function<LitebridgeContext.Mode, LitebridgeContext> creator = invocation.getArgument(1);
            creator.apply(LitebridgeContext.Mode.SQL);
            return expectedResult;
        });

        final Function<InsertAllStep, InsertAllStep> stepFunction = step -> step;

        // When
        final InsertResult result = litebridgeOracle.insertAll(stepFunction);

        // Then
        assertSame(expectedResult, result);
        verify(insertAllEngine).insertAll(any(), any());
    }
}
