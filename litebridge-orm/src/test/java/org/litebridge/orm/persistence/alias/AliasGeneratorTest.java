package org.litebridge.orm.persistence.alias;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.persistence.OrmTable;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AliasGeneratorTest {

    @Test
    void noOpAliasGenerator() {
        NoOpAliasGenerator generator = new NoOpAliasGenerator();
        Table table = new Table("cat", "sch", "tab");
        TableMetaData tableMetaData = new TableMetaData(table, List.of(), List.of());
        OrmTable ormTable = mock(OrmTable.class);
        when(ormTable.getMetaData()).thenReturn(tableMetaData);
        
        assertEquals(table, generator.aliasTable(ormTable));
        
        ColumnMetaData columnMetaData = new ColumnMetaData(table, "col", false, Types.VARCHAR, 0);
        Column column = generator.aliasColumn(table, columnMetaData);
        assertEquals(table, column.table());
        assertEquals("col", column.name());
    }
}
