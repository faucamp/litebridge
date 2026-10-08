package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Delete;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PreparedOperationTest {

    @Test
    void recordProperties() {
        // Given
        final ConditionGroup emptyWhere = new ConditionGroup(Collections.emptyList());
        final Operation op1 = new Delete(new Table("T1"), emptyWhere);
        final Operation op2 = new Delete(new Table("T2"), emptyWhere);
        final List<BindValue> bindValues = List.of(new BindValue("val", 12));

        final PreparedOperation prep1 = new PreparedOperation(op1, bindValues);
        final PreparedOperation prep2 = new PreparedOperation(op1, bindValues);
        final PreparedOperation prepDiff = new PreparedOperation(op2, bindValues);

        // When & Then
        assertEquals(op1, prep1.operation());
        assertEquals(bindValues, prep1.bindValues());

        assertEquals(prep1, prep2);
        assertEquals(prep1.hashCode(), prep2.hashCode());
        assertNotEquals(prep1, prepDiff);
    }
}
