package org.litebridge.orm.nativesql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeSqlCacheTest {

    @Test
    void getCachedSql() {
        // Given
        final NativeSqlCache cache = new NativeSqlCache();

        // When
        final ParsedSql parsedSql = cache.getCachedSql("SELECT * FROM schema.table WHERE id = :id");

        // Then
        assertNotNull(parsedSql);
        assertEquals("SELECT * FROM schema.table WHERE id = ?", parsedSql.sql());
        assertEquals(1, parsedSql.bindParameterNames().size());
        assertEquals("id", parsedSql.bindParameterNames().getFirst());
    }
}