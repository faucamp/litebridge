package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteEngineTest {

    static class UserDto {
        private Long id;
    }

    @Test
    void operationTypeNameAndLogger() {
        // Given
        final DeleteEngine engine = new DeleteEngine();

        // When / Then
        assertEquals("DELETE", engine.operationTypeName());
        assertNotNull(engine.logger());
    }

    @Test
    void createDeleteNodeChainForDto() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final QueryNode node = DeleteEngine.createDeleteNodeChain(UserDto.class, d -> d, context);

        // Then
        assertNotNull(node);
    }

    @Test
    void createDeleteNodeChainForTable() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final QueryNode node = DeleteEngine.createDeleteNodeChain("users", d -> d, context);

        // Then
        assertNotNull(node);
    }

    @Test
    void deleteDtoClass() throws Exception {
        // Given
        final DeleteEngine engine = new DeleteEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final Delete operation = mock(Delete.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("DELETE FROM users");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When
        final UpdateResult result = engine.delete(UserDto.class, d -> d, context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    @Test
    void deleteTableName() throws Exception {
        // Given
        final DeleteEngine engine = new DeleteEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final Delete operation = mock(Delete.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("DELETE FROM users");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When
        final UpdateResult result = engine.delete("users", d -> d, context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }
}
