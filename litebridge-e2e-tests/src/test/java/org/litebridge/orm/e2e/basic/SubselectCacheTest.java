package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.engine.QueryPlanCache;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MultiDbTestExtension.class)
public class SubselectCacheTest extends AbstractE2eTest {

    @TestTemplate
    public void testSubselectCaching(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Litebridge lb = (Litebridge) litebridge;
        final QueryPlanCache cache = LitebridgeInspector.getQueryPlanCache(lb);

        // Clear cache if needed (usually fresh context anyway)
        cache.clear();

        final String personTable = tableMapper.qualifyName("PERSON");
        final String idCol = tableMapper.transformColumnName("PERSON_ID");
        final String nameCol = tableMapper.transformColumnName("FIRST_NAME");
        final String ageCol = tableMapper.transformColumnName("AGE");

        // Given
        // Query 1
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name1")).list();
        final int size1 = cache.size();
        assertEquals(1, size1, "Should have 1 cached query");

        // When
        // Query 2: Same structure, different bind value in subquery
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name2")).list();
        final int size2 = cache.size();
        assertEquals(1, size2, "Should still have 1 cached query (hit)");

        // Query 3: Different structure in subquery
        lb.select(idCol).from(personTable).where(idCol).eq(q -> q.select(idCol).from(personTable).where(nameCol).eq("Name1").and(ageCol).gt(20)).list();
        final int size3 = cache.size();
        assertEquals(2, size3, "Should have 2 cached queries (miss)");
    }
}
