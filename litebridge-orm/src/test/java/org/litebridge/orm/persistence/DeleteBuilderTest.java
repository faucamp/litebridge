package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.Delete;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeleteBuilderTest {

    @Test
    void testBuild() {
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = new TableMetaData("catalog", "schema", "table", Collections.emptyList(), Collections.emptyList());
        when(table.getMetaData()).thenReturn(metaData);

        final DeleteBuilder builder = new DeleteBuilder(table);
        final ConditionGroup conditions = mock(ConditionGroup.class);
        builder.where(conditions);

        final Delete delete = builder.build();
        assertNotNull(delete);
        assertEquals("table", delete.table().name());
        assertEquals("schema", delete.table().schema());
        assertEquals("catalog", delete.table().catalog());
        assertEquals(conditions, delete.where());
    }

    @Test
    void testBuildWithoutConditions() {
        final OrmTable table = mock(OrmTable.class);
        final DeleteBuilder builder = new DeleteBuilder(table);

        assertThrows(NullPointerException.class, builder::build);
    }
}
