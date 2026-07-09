package org.litebridgedb.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.ConditionGroup;
import org.litebridgedb.db.spi.query.LogicCondition;
import org.litebridgedb.db.spi.query.LogicOperator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DeleteTest {

    @Test
    void testRecord() {
        // Given
        final Table table = new Table("CAT", "SCHEMA", "TEST");
        final Condition condition = mock(Condition.class);
        final ConditionGroup where = new ConditionGroup(new LogicCondition(LogicOperator.AND, condition));

        // When
        final Delete delete = new Delete(table, where);

        // Then
        assertEquals(table, delete.table());
        assertEquals(where, delete.where());
    }
}
