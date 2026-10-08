package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertTest {

    @Test
    void recordProperties() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final List<UpdateColumn> columns = List.of(new UpdateColumn("COL1"));
        final Insert insert1 = new Insert(table, columns, 1, true);
        final Insert insert2 = new Insert(table, columns, 1, true);
        final Insert insertDiff = new Insert(table, columns, 2, false);

        // When & Then
        assertEquals(table, insert1.table());
        assertEquals(columns, insert1.columns());
        assertEquals(1, insert1.rows());
        assertTrue(insert1.returnGeneratedKeys());

        assertEquals(insert1, insert2);
        assertEquals(insert1.hashCode(), insert2.hashCode());
        assertNotEquals(insert1, insertDiff);
    }
}
