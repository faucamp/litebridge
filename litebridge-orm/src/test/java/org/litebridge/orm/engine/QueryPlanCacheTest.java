package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class QueryPlanCacheTest {

    @Test
    void testCacheByQueryNode() {
        // Given
        final QueryPlanCache cache = new QueryPlanCache();
        final String sql = "SELECT 1";
        final QueryNode node = new SelectNode(null, null, null, null, new ExpressionSpec[0], new Class[]{Object.class});
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation(sql, Collections.emptyList(), null, null);

        // When / Then
        assertNull(cache.get(node));
        assertEquals(0, cache.size());

        cache.put(node, cachedOperation);
        assertEquals(1, cache.size());
        assertNotNull(cache.get(node));
        assertSame(cachedOperation, cache.get(node));
        assertSame(sql, cache.get(node).sql());

        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get(node));
    }

    @Test
    void testCacheByNodeHash() {
        // Given
        final QueryPlanCache cache = new QueryPlanCache();
        final String sql = "SELECT * FROM users WHERE id = ?";
        final int hash = 12345;
        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation(sql, List.of(Types.INTEGER), null, null);

        // When / Then
        assertNull(cache.get(hash));

        cache.put(hash, cachedOperation);
        assertEquals(1, cache.size());
        assertSame(cachedOperation, cache.get(hash));

        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get(hash));
    }

    @Test
    void testCachedOperationPreparedSql() {
        // Given
        final String sql = "INSERT INTO users (id, name) VALUES (?, ?)";
        final List<Integer> sqlTypes = List.of(Types.INTEGER, Types.VARCHAR);
        final TypeConversionMetaData typeConversionMetaData = mock(TypeConversionMetaData.class);
        final UpdateMetaData updateMetaData = mock(UpdateMetaData.class);

        final QueryPlanCache.CachedOperation operation = new QueryPlanCache.CachedOperation(
                sql,
                sqlTypes,
                typeConversionMetaData,
                updateMetaData
        );

        // When
        final PreparedSql preparedSql = operation.preparedSql(List.of(42, "Alice"));

        // Then
        assertEquals(sql, operation.sql());
        assertEquals(sqlTypes, operation.bindValueSqlTypes());
        assertSame(typeConversionMetaData, operation.typeConversionMetaData());
        assertSame(updateMetaData, operation.updateMetaData());

        assertEquals(sql, preparedSql.sql());
        assertSame(typeConversionMetaData, preparedSql.typeConversionMetaData());
        assertSame(updateMetaData, preparedSql.updateMetaData());
        assertEquals(2, preparedSql.bindValues().size());

        final BindValue first = preparedSql.bindValues().get(0);
        assertEquals(42, first.value());
        assertEquals(Types.INTEGER, first.sqlDataType());

        final BindValue second = preparedSql.bindValues().get(1);
        assertEquals("Alice", second.value());
        assertEquals(Types.VARCHAR, second.sqlDataType());
    }

    @Test
    void testCachedOperationPreparedSqlThrowsOnSizeMismatch() {
        // Given
        final String sql = "SELECT * FROM users WHERE id = ?";
        final QueryPlanCache.CachedOperation operation = new QueryPlanCache.CachedOperation(
                sql,
                List.of(Types.INTEGER),
                null,
                null
        );

        // When / Then
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> operation.preparedSql(List.of(1, 2))
        );
        assertEquals("Number of bind values does not match number of bind value SQL types; expected 1, got 2", exception.getMessage());
    }
}
