package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.RowValue;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertBuilderTest {

    @Test
    void testBuildSimple() {
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(table.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(new org.litebridge.db.spi.Table("TABLE"));
        when(metaData.primaryKey()).thenReturn(Collections.emptyList());

        final InsertBuilder builder = new InsertBuilder(table);
        final RowValue rowValue = new RowValue(Collections.emptyList());
        builder.add(new DtoRowValue(new Object(), rowValue));

        final Insert insert = builder.build();
        assertNotNull(insert);
        assertEquals("TABLE", insert.table().name());
        assertEquals(1, insert.rows().size());
        assertFalse(insert.returnGeneratedKeys());
    }

    @Test
    void testReturnGeneratedKeysAutoIncrement() {
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(table.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(new org.litebridge.db.spi.Table("TABLE"));

        final ColumnMetaData pk = mock(ColumnMetaData.class);
        when(pk.name()).thenReturn("ID");
        when(pk.isAutoIncrement()).thenReturn(true);
        when(metaData.primaryKey()).thenReturn(List.of(pk));

        final InsertBuilder builder = new InsertBuilder(table);
        
        // No value for ID
        final RowValue rowValue1 = new RowValue(List.of(new ColumnValue(mock(org.litebridge.db.spi.Column.class), "value")));
        when(rowValue1.columns().getFirst().column().name()).thenReturn("OTHER");
        builder.add(new DtoRowValue(new Object(), rowValue1));

        assertTrue(builder.build().returnGeneratedKeys());

        // Value for ID provided
        final InsertBuilder builder2 = new InsertBuilder(table);
        final org.litebridge.db.spi.Column idColumn = mock(org.litebridge.db.spi.Column.class);
        when(idColumn.name()).thenReturn("ID");
        final RowValue rowValue2 = new RowValue(List.of(new ColumnValue(idColumn, 123L)));
        builder2.add(new DtoRowValue(new Object(), rowValue2));

        assertFalse(builder2.build().returnGeneratedKeys());
    }

    @Test
    void testReturnGeneratedKeysSequence() {
        final OrmTable table = mock(OrmTable.class);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(table.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(new org.litebridge.db.spi.Table("TABLE"));

        final ColumnMetaData pk = mock(ColumnMetaData.class);
        when(pk.name()).thenReturn("ID");
        when(pk.getGenerator()).thenReturn(mock(SequenceColumnValueGenerator.class));
        when(metaData.primaryKey()).thenReturn(List.of(pk));

        final InsertBuilder builder = new InsertBuilder(table);
        builder.add(new DtoRowValue(new Object(), new RowValue(Collections.emptyList())));

        assertTrue(builder.build().returnGeneratedKeys());
    }
}
