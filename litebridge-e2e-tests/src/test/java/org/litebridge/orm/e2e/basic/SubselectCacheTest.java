package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.engine.QueryPlanCache;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MultiDbTestExtension.class)
public class SubselectCacheTest extends AbstractE2eTest {

    @TestTemplate
    public void testSubselectCaching(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Litebridge lb = litebridge;
        final QueryPlanCache cache = getCache(lb);

        // Clear cache if needed (usually fresh context anyway)
        clearCache(cache);

        String personTable = tableMapper.qualifyName("PERSON");
        String idCol = tableMapper.transformColumnName("PERSON_ID");
        String nameCol = tableMapper.transformColumnName("FIRST_NAME");
        String ageCol = tableMapper.transformColumnName("AGE");

        // Given
        // Query 1
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name1")).list();
        int size1 = getCacheSize(cache);
        assertEquals(1, size1, "Should have 1 cached query");

        // When
        // Query 2: Same structure, different bind value in subquery
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name2")).list();
        int size2 = getCacheSize(cache);
        assertEquals(1, size2, "Should still have 1 cached query (hit)");

        // Query 3: Different structure in subquery
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name1").and(ageCol).gt(20)).list();
        int size3 = getCacheSize(cache);
        assertEquals(2, size3, "Should have 2 cached queries (miss)");
    }

    private QueryPlanCache getCache(Litebridge lb) throws Exception {
        Field field = Litebridge.class.getDeclaredField("queryPlanCache");
        field.setAccessible(true);
        return (QueryPlanCache) field.get(lb);
    }

    private int getCacheSize(QueryPlanCache cache) throws Exception {
        Field field = QueryPlanCache.class.getDeclaredField("cache");
        field.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) field.get(cache);
        return map.size();
    }

    private void clearCache(QueryPlanCache cache) throws Exception {
        Field field = QueryPlanCache.class.getDeclaredField("cache");
        field.setAccessible(true);
        Map<?, ?> map = (Map<?, ?>) field.get(cache);
        map.clear();
    }
}
