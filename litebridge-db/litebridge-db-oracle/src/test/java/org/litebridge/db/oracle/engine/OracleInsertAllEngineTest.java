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
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;

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
    void insertAll_executesSuccessfully() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);

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
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(InsertResult.class), eq(transactionManager));
    }

    @Test
    void insertAll_whenDatabaseThrowsSQLException_throwsIllegalStateException() throws SQLException {
        // Given
        final LitebridgeContext litebridgeContext = mock(LitebridgeContext.class);
        final QueryCompiler queryCompiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);

        when(litebridgeContext.createQueryCompiler()).thenReturn(queryCompiler);
        when(litebridgeContext.databaseProvider()).thenReturn(databaseProvider);
        when(litebridgeContext.transactionManager()).thenReturn(transactionManager);

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
    void metadataMethods() {
        assertEquals("INSERT ALL", insertAllEngine.operationTypeName());
        assertNotNull(insertAllEngine.logger());
    }
}
