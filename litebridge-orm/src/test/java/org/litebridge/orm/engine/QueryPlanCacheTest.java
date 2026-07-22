package org.litebridge.orm.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;

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

        // When / Then
        assertNull(cache.get(select1));

        cache.put(select1, preparedSql);
        assertSame(preparedSql, cache.get(select1));

        // Structural match
        final Select select2 = new Select(table, List.of(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty(), List.of(), java.util.Optional.empty());
        assertSame(preparedSql, cache.get(select2));
    }
}
