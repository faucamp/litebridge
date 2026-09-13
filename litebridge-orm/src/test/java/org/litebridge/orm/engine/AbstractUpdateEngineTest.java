package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractUpdateEngineTest {

    static class TestUpdateEngine extends AbstractInsertEngine {
        private final Logger logger = LoggerFactory.getLogger(TestUpdateEngine.class);

        @Override
        protected String operationTypeName() {
            return "TEST_OP";
        }

        @Override
        protected Logger logger() {
            return logger;
        }
    }

    @Test
    void emptyUpdateMetaDataConstant() {
        // Given / When / Then
        assertNotNull(AbstractUpdateEngine.EMPTY_UPDATE_META_DATA);
        assertFalse(AbstractUpdateEngine.EMPTY_UPDATE_META_DATA.returnGeneratedKeys());
        assertEquals(1, AbstractUpdateEngine.EMPTY_UPDATE_META_DATA.rows());
        assertEquals(0, AbstractUpdateEngine.EMPTY_UPDATE_META_DATA.bindValueColumns());
    }

    @Test
    void executeWithDefaultMetaDataCacheMissAndHit() throws Exception {
        // Given
        final TestUpdateEngine engine = new TestUpdateEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final QueryNode node = new UpdateNode(null, "users", null);
        final Update operation = mock(Update.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(node)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("UPDATE users SET name = 'Bob'");

        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When: First execution (cache miss)
        final UpdateResult result1 = engine.execute(node, context);

        // Then: Cache populated and executed
        assertSame(updateResult, result1);
        assertEquals(1, queryPlanCache.size());
        verify(compiler, times(1)).compile(node);

        // When: Second execution (cache hit)
        final UpdateResult result2 = engine.execute(node, context);

        // Then: Returned from cache, compiler not called again
        assertSame(updateResult, result2);
        verify(compiler, times(1)).compile(node);
        verify(databaseProvider, times(2)).executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager));
    }

    @Test
    void executeWithCustomUpdateMetaDataCreator() throws Exception {
        // Given
        final TestUpdateEngine engine = new TestUpdateEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final QueryNode node = new UpdateNode(null, "orders", null);
        final Update operation = mock(Update.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, List.of(new BindValue(1, Types.INTEGER)));
        when(compiler.compile(node)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("UPDATE orders SET status = ?");

        final UpdateMetaData customMetaData = new UpdateMetaData(true, null, new String[]{"id"}, 1, 0);
        final UpdateResult updateResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager))).thenReturn(updateResult);

        // When
        final UpdateResult result = engine.execute(node, p -> customMetaData, UpdateResult.class, context);

        // Then
        assertSame(updateResult, result);
        final QueryPlanCache.CachedOperation cached = queryPlanCache.get(node.hashCode());
        assertNotNull(cached);
        assertSame(customMetaData, cached.updateMetaData());
    }

    @Test
    void executeThrowsIllegalStateExceptionOnSQLException() throws Exception {
        // Given
        final TestUpdateEngine engine = new TestUpdateEngine();
        final LitebridgeContext context = mock(LitebridgeContext.class);
        final QueryCompiler compiler = mock(QueryCompiler.class);
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager txManager = mock(TransactionManager.class);
        final QueryPlanCache queryPlanCache = new QueryPlanCache();

        when(context.createQueryCompiler()).thenReturn(compiler);
        when(context.databaseProvider()).thenReturn(databaseProvider);
        when(context.transactionManager()).thenReturn(txManager);
        when(context.queryPlanCache()).thenReturn(queryPlanCache);

        final QueryNode node = new UpdateNode(null, "users", null);
        final Update operation = mock(Update.class);
        final PreparedOperation preparedOperation = new PreparedOperation(operation, Collections.emptyList());
        when(compiler.compile(node)).thenReturn(preparedOperation);
        when(databaseProvider.toSql(operation, txManager)).thenReturn("DELETE FROM users");
        when(databaseProvider.executeUpdate(any(PreparedSql.class), eq(UpdateResult.class), eq(txManager)))
                .thenThrow(new SQLException("Deadlock detected"));

        // When / Then
        final IllegalStateException ex = assertThrows(IllegalStateException.class, () -> engine.execute(node, context));
        assertTrue(ex.getMessage().contains("Failed to execute TEST_OP: DELETE FROM users"));
        assertInstanceOf(SQLException.class, ex.getCause());
    }
}
