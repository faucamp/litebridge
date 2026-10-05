package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MappingPlanCacheTest {

    @Test
    void keyEqualityAndHashCode() {
        // Given
        final MappingPlanKey key1 = new MappingPlanKey(String.class, Integer.class, 12345);
        final MappingPlanKey key2 = new MappingPlanKey(String.class, Integer.class, 12345);
        final MappingPlanKey key3 = new MappingPlanKey(String.class, null, 12345);
        final MappingPlanKey key4 = new MappingPlanKey(Long.class, Integer.class, 12345);
        final MappingPlanKey key5 = new MappingPlanKey(String.class, Integer.class, 99999);

        // Then
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1, key3);
        assertNotEquals(key1, key4);
        assertNotEquals(key1, key5);
        assertEquals(String.class, key1.dtoClass());
        assertEquals(Integer.class, key1.contextDtoClass());
        assertEquals(12345, key1.rowStructureHash());
    }

    @Test
    void putGetSizeClear() {
        // Given
        final MappingPlanCache cache = new MappingPlanCache();
        final MappingPlanKey key1 = new MappingPlanKey(String.class, null, 100);
        final MappingPlanKey key2 = new MappingPlanKey(Long.class, null, 200);

        final DtoMapper.MappingPlan plan1 = new DtoMapper.MappingPlan(Map.of(), null);
        final DtoMapper.MappingPlan plan2 = new DtoMapper.MappingPlan(Collections.emptyMap(), null);

        // When & Then
        assertEquals(0, cache.size());
        assertNull(cache.get(key1));

        cache.put(key1, plan1);
        assertEquals(1, cache.size());
        assertSame(plan1, cache.get(key1));

        cache.put(key2, plan2);
        assertEquals(2, cache.size());
        assertSame(plan2, cache.get(key2));
        assertNotNull(cache.cache());

        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get(key1));
        assertNull(cache.get(key2));
    }
}
