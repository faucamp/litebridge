package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateOpResult;
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

class UpdateEngineTest {

    @Test
    void operationTypeNameAndLogger() {
        // Given
        final UpdateEngine engine = new UpdateEngine();

        // When / Then
        assertEquals("UPDATE", engine.operationTypeName());
        assertNotNull(engine.logger());
    }

    @Test
    void createUpdateNodeChainForDto() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final QueryNode node = UpdateEngine.createUpdateNodeChain(UserDto.class, s -> s.set("name").to("Alice"), context);

        // Then
        assertNotNull(node);
    }

    @Test
    void createUpdateNodeChainForTable() {
        // Given
        final LitebridgeContext context = mock(LitebridgeContext.class);

        // When
        final QueryNode node = UpdateEngine.createUpdateNodeChain("users", s -> s.set("name").to("Alice"), context);

        // Then
        assertNotNull(node);
    }

    @Test
    void updateDtoClass() throws Exception {
        // Given
        final UpdateEngine engine = new UpdateEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final Update operation = mock(Update.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("UPDATE users SET name = 'Alice'");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When
        final UpdateOpResult result = engine.update(UserDto.class, s -> s.set("name").to("Alice"), context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    @Test
    void updateTableName() throws Exception {
        // Given
        final UpdateEngine engine = new UpdateEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final Update operation = mock(Update.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(any(QueryNode.class))).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("UPDATE users SET name = 'Alice'");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When
        final UpdateOpResult result = engine.update("users", s -> s.set("name").to("Alice"), context);

        // Then
        assertSame(updateResult, result);
        verify(databaseProvider).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    static class UserDto {
        private String name;
    }
}
