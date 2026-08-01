package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class QueryPlanCacheTest {

    @Test
    void testCache() {
        // Given
        final QueryPlanCache cache = new QueryPlanCache();
        final Table table = new Table("TEST_TABLE");
        final Select select1 = new Select(table, List.of(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty());
        final PreparedSql preparedSql = new PreparedSql("SELECT 1", List.of());
        final QueryNode node = new SelectNode(null, new ExpressionSpec[0], Object.class);
        final QueryPlanCache.CachedOperation cachedOperation =  new QueryPlanCache.CachedOperation(preparedSql, select1, null);

        // When / Then
        assertNull(cache.get(node));

        cache.put(node, cachedOperation);
        assertNotNull(cache.get(node));
        assertSame(cachedOperation, cache.get(node));

        // Structural match
        final Select select2 = new Select(table, List.of(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty());
        assertSame(preparedSql, cache.get(node));
    }
}
