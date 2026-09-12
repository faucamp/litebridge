package org.litebridge.db.oracle.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.oracle.sql.OracleInsertSqlGenerator;
import org.litebridge.db.oracle.sql.OracleMathOperationGenerator;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleInsertAllEngineTest {

    private OracleInsertAllEngine insertAllEngine;

    @BeforeEach
    void setUp() {
        final OracleColumnIdentifierGenerator columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        final OracleMathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(columnIdentifierGenerator);
        final OracleInsertSqlGenerator sqlGenerator = new OracleInsertSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                (t, c) -> mock(TableMetaData.class));
        insertAllEngine = new OracleInsertAllEngine(sqlGenerator);
    }

    @Test
    void insertAll_whenCacheMiss_compilesAndCachesAndExecutesSuccessfully() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);
        when(litebridgeContext.queryPlanCache()).thenReturn(queryPlanCache);

        final Insert insert = new Insert(new Table("ACCOUNT"), List.of(new UpdateColumn("NAME")), 1, false);
        final PreparedOperation preparedOperation = new PreparedOperation(insert, List.of(new BindValue("Alice", Types.VARCHAR)));
        when(queryCompiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);

        final InsertResult expectedResult = mock(InsertResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(transactionManager)))
                .thenReturn(expectedResult);

        // When
        final InsertResult result = insertAllEngine.insertAll(
                step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Alice")),
                mode -> litebridgeContext);

        // Then
        assertSame(expectedResult, result);
        assertEquals(1, queryPlanCache.size());

        final ArgumentCaptor<PreparedSql> sqlCaptor = ArgumentCaptor.forClass(PreparedSql.class);
        verify(databaseProvider).executeUpdate(sqlCaptor.capture(), eq(InsertResult.class), eq(transactionManager));
        final PreparedSql preparedSql = sqlCaptor.getValue();
        assertEquals("INSERT ALL INTO ACCOUNT (NAME) VALUES (?) SELECT * FROM DUAL", preparedSql.sql());
        assertEquals(1, preparedSql.bindValues().size());
        assertEquals("Alice", preparedSql.bindValues().getFirst().value());
        assertEquals(Types.VARCHAR, preparedSql.bindValues().getFirst().sqlDataType());
        verify(queryCompiler, times(1)).compile(any(QueryNode.class));
    }

    @Test
    void insertAll_whenCacheHit_reusesCachedOperationAndExtractsNewBindValues() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);
        when(litebridgeContext.queryPlanCache()).thenReturn(queryPlanCache);

        final Insert insert = new Insert(new Table("ACCOUNT"), List.of(new UpdateColumn("NAME")), 1, false);
        final PreparedOperation preparedOperation = new PreparedOperation(insert, List.of(new BindValue("Alice", Types.VARCHAR)));
        when(queryCompiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);

        final InsertResult firstResult = mock(InsertResult.class);
        final InsertResult secondResult = mock(InsertResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(transactionManager)))
                .thenReturn(firstResult, secondResult);

        // When - First execution (cache miss)
        final InsertResult result1 = insertAllEngine.insertAll(
                step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Alice")),
                mode -> litebridgeContext);

        // When - Second execution with same AST structure but different bind value (cache hit)
        final InsertResult result2 = insertAllEngine.insertAll(
                step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Bob")),
                mode -> litebridgeContext);

        // Then
        assertSame(firstResult, result1);
        assertSame(secondResult, result2);
        assertEquals(1, queryPlanCache.size());

        // Compiler should only have been invoked on the first call (cache miss)
        verify(queryCompiler, times(1)).compile(any(QueryNode.class));

        final ArgumentCaptor<PreparedSql> sqlCaptor = ArgumentCaptor.forClass(PreparedSql.class);
        verify(databaseProvider, times(2)).executeUpdate(sqlCaptor.capture(), eq(InsertResult.class), eq(transactionManager));

        final List<PreparedSql> executedSqls = sqlCaptor.getAllValues();
        assertEquals("INSERT ALL INTO ACCOUNT (NAME) VALUES (?) SELECT * FROM DUAL", executedSqls.get(0).sql());
        assertEquals("Alice", executedSqls.get(0).bindValues().getFirst().value());

        assertEquals("INSERT ALL INTO ACCOUNT (NAME) VALUES (?) SELECT * FROM DUAL", executedSqls.get(1).sql());
        assertEquals("Bob", executedSqls.get(1).bindValues().getFirst().value());
        assertEquals(Types.VARCHAR, executedSqls.get(1).bindValues().getFirst().sqlDataType());
    }

    @Test
    void insertAll_whenDatabaseThrowsSQLException_throwsIllegalStateException() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);
        when(litebridgeContext.queryPlanCache()).thenReturn(queryPlanCache);

        final Insert insert = new Insert(new Table("ACCOUNT"), List.of(new UpdateColumn("NAME")), 1, false);
        final PreparedOperation preparedOperation = new PreparedOperation(insert, List.of(new BindValue("Alice", Types.VARCHAR)));
        when(queryCompiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);

        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(transactionManager)))
                .thenThrow(new SQLException("connection closed"));

        // When / Then
        final IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> insertAllEngine.insertAll(
                        step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Alice")),
                        mode -> litebridgeContext));
        assertEquals("Failed to execute INSERT ALL: INSERT ALL INTO ACCOUNT (NAME) VALUES (?) SELECT * FROM DUAL", ex.getMessage());
    }

    @Test
    void insertAll_whenCacheHitAndDatabaseThrowsSQLException_throwsIllegalStateException() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);
        when(litebridgeContext.queryPlanCache()).thenReturn(queryPlanCache);

        final Insert insert = new Insert(new Table("ACCOUNT"), List.of(new UpdateColumn("NAME")), 1, false);
        final PreparedOperation preparedOperation = new PreparedOperation(insert, List.of(new BindValue("Alice", Types.VARCHAR)));
        when(queryCompiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);

        final InsertResult firstResult = mock(InsertResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(transactionManager)))
                .thenReturn(firstResult)
                .thenThrow(new SQLException("database error during cached execution"));

        // First execution populates the cache
        insertAllEngine.insertAll(
                step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Alice")),
                mode -> litebridgeContext);

        // When / Then - Second execution hits cache and throws SQLException
        final IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> insertAllEngine.insertAll(
                        step -> step.intoTable("ACCOUNT", s -> s.into("NAME").values("Bob")),
                        mode -> litebridgeContext));
        assertEquals("Failed to execute INSERT ALL: INSERT ALL INTO ACCOUNT (NAME) VALUES (?) SELECT * FROM DUAL", ex.getMessage());
    }

    @Test
    void metadataMethods() {
        assertEquals("INSERT ALL", insertAllEngine.operationTypeName());
        assertNotNull(insertAllEngine.logger());
    }
}
