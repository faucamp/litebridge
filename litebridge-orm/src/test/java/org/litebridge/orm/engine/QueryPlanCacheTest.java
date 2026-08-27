//package org.litebridge.orm.engine;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.orm.api.select.ast.QueryNode;
//import org.litebridge.orm.api.select.ast.SelectNode;
//import org.litebridge.orm.expression.ExpressionSpec;
//
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.junit.jupiter.api.Assertions.assertSame;
//
//class QueryPlanCacheTest {
//
//    @Test
//    void testCache() {
//        // Given
//        final QueryPlanCache cache = new QueryPlanCache();
//        final String sql = "SELECT 1";
//        final QueryNode node = new SelectNode(null, null, null, new ExpressionSpec[0], Object.class);
//        final QueryPlanCache.CachedOperation cachedOperation = new QueryPlanCache.CachedOperation(sql, Collections.emptyList(), null, null, null);
//
//        // When / Then
//        assertNull(cache.get(node));
//
//        cache.put(node, cachedOperation);
//        assertNotNull(cache.get(node));
//        assertSame(cachedOperation, cache.get(node));
//
//        // Structural match
//        assertNotNull(cache.get(node));
//        assertSame(sql, cache.get(node).sql());
//    }
//}
