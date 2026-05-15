package org.litebridgedb.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Condition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DeleteTest {

    @Test
    void testRecord() {
        Table table = new Table("CAT", "SCHEMA", "TEST");
        Condition condition = mock(Condition.class);
        List<Condition> where = List.of(condition);
        
        Delete delete = new Delete(table, where);
        
        assertEquals(table, delete.table());
        assertEquals(where, delete.where());
    }
}
